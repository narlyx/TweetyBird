package dev.narlyx.tweetybird;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

/**
 * This is the main class behind TweetyBird, use this class to setup, start, and use TweetyBird.
 */
public class TweetyBird {

  /**
   * An interface for TweetyBird to define the structure of a odometer class
   * used to receive data on the robot's current position.
   */
  public interface Odometer {
    double getX();
    double getY();
    double getZ();
    void resetTo(double x, double y, double z);
  }

  /**
   * An interface for TweetyBird to define the structure of a driver class
   * used to control movement.
   */
  public interface Driver {
    void setHeading(double axial, double lateral, double yaw, double speed);
    void stopAndHold();
  }

  // Creating variables to be populated from the builder
  protected final LinearOpMode opMode;
  protected final Odometer odometer;
  protected final Driver driver;
  protected final WaypointQueue waypointQueue;
  protected final double minSpeed, maxSpeed, distanceBuffer, rotationBuffer;
  protected final double speedModifier, correctionOverpower;
  protected final boolean debuggingEnabled;

  // Other classes used by TweetyBird
  protected final Runtime runtime;

  /**
   * Creates a new waypoint and adds it to the end of the queue
   * @param x New target X
   * @param y New target Y
   * @param z New target Z
   */
  public void sendTargetPosition(double x, double y, double z) {
    waypointQueue.addWaypoint(new Waypoint(x,y,Math.toRadians(z)));
  }

  /**
   * Shortcut to reset the position in the provided odometer
   * @param x New x
   * @param y New y
   * @param z New z
   */
  public void resetPosition(double x, double y, double z) {
    odometer.resetTo(x,y,z);
  }

  /**
   * Returns weather the mover is currently in progress or not
   * @return Busy
   */
  public boolean isBusy() {
    return waypointQueue.getSize()>1;
  }

  /**
   * Will wait in a while loop until isBusy() is false
   */
  public void waitWhileBusy() {
    if (opMode != null) {
      while (isBusy() && opMode.opModeIsActive());
    } else {
      while (isBusy());
    }
  }

  /**
   * Terminates TweetyBird
   */
  public void close() {
    runtime.interrupt();
  }

  /**
   * Internal method used to send debug messages
   * @param message message to be sent
   */
  protected void sendDebugMessage(String message) {
    if (debuggingEnabled) {
      if (opMode == null) {
        System.out.println("[TweetyBird]: "+message);
      } else {
        opMode.telemetry.addLine("[TweetyBird]: "+message);
        opMode.telemetry.setAutoClear(false);
        opMode.telemetry.update();
      }
    }
  }

  /**
   * Do NOT call this method yourself, instead use the builder to start TweetyBird
   * @param builder Passed builder
   */
  public TweetyBird(Builder builder) {
    // Pulling configuration from the builder
    this.opMode = builder.opMode;
    this.odometer = builder.odometer;
    this.driver = builder.driver;
    this.minSpeed = builder.minSpeed;
    this.maxSpeed = builder.maxSpeed;
    this.speedModifier = 0.05;
    this.correctionOverpower = 5;
    this.distanceBuffer = builder.distanceBuffer;
    this.rotationBuffer = Math.toRadians(builder.rotationBuffer);
    this.debuggingEnabled = builder.debuggingEnabled;

    // Setting up queue
    waypointQueue = new WaypointQueue(this);

    // Starting runtime
    runtime = new Runtime(this);
    runtime.start();

    sendDebugMessage("Initial setup complete!\n");
  }

  /**
   * Use this method to properly set up TweetyBird using Java's builder pattern.
   */
  public static class Builder {

    private LinearOpMode opMode = null;
    /**
     * REQUIRED FOR FTC
     * Sets the LinearOpMode that TweetyBird will use,
     * this will be used to start and stop TweetyBird.
     * @param opMode Passed OpMode
     * @return Updated builder
     */
    public Builder setLinearOpMode(LinearOpMode opMode) {
      this.opMode = opMode;
      return this;
    }

    private Odometer odometer = null;
    /**
     * REQUIRED
     * A new class that implements TweetyBird's Odometer interface,
     * TweetyBird will use this class to receive input on the robot's location.
     * @param odometer Passed Odometer
     * @return Updated builder
     */
    public Builder setOdometer(Odometer odometer) {
      this.odometer = odometer;
      return this;
    }

    private Driver driver = null;
    /**
     * REQUIRED
     * A new class that implements TweetyBird's Driver interface,
     * TweetyBird will use this class to output motion.
     * @param driver Passed Odometer
     * @return Updated builder
     */
    public Builder setDriver(Driver driver) {
      this.driver = driver;
      return this;
    }

    private double minSpeed = .1;
    /**
     * OPTIONAL
     * This value will define how slow TweetyBird is allowed to move,
     * the default value is 0.1.
     * @param minSpeed A number between 0 and 1
     * @return Updated builder
     */
    public Builder setMinimumSpeed(double minSpeed) {
      this.minSpeed = minSpeed;
      return this;
    }

    private double maxSpeed = 1;
    /**
     * OPTIONAL
     * This value will define how fast TweetyBird is allowed to move,
     * the default value is 1.
     * @param maxSpeed A number between 0 and 1, must be greater than the minimum speed
     * @return Updated builder
     */
    public Builder setMaximumSpeed(double maxSpeed) {
      this.maxSpeed = maxSpeed;
      return this;
    }

    private double distanceBuffer = 1;
    /**
     * OPTIONAL
     * This value will define how close TweetyBird needs to move be to its target to consider
     * itself to be okay to stop moving,
     * the default value is 1.
     * @param distanceBuffer Number of units of measurement
     * @return Updated builder
     */
    public Builder setDistanceBuffer(double distanceBuffer) {
      this.distanceBuffer = distanceBuffer;
      return this;
    }

    private double rotationBuffer = 1;
    /**
     * OPTIONAL
     * This value will define how close TweetyBird needs to rotate to its target to consider
     * itself to be okay to stop rotating,
     * the default value is 1 degree.
     * @param rotationBuffer Number in degrees
     * @return Updated builder
     */
    public Builder setRotationBuffer(double rotationBuffer) {
      this.rotationBuffer = rotationBuffer;
      return this;
    }

    private boolean debuggingEnabled = false;

    /**
     * NOT RECOMMENDED
     * This wil flood your console with debug messages, only use for development
     * and will slow the runtime loop without a linear op mode,
     * the default value is false
     * @param debuggingEnabled Weather to enable debug logs
     * @return Updated builder
     */
    public Builder setDebuggingEnabled(boolean debuggingEnabled) {
      this.debuggingEnabled = debuggingEnabled;
      return this;
    }

    /**
     * Will construct TweetyBird with the configuration defined within this builder and return
     * a new copy of TweetyBird.
     * @return TweetyBird
     */
    public TweetyBird build() {
      return new TweetyBird(this);
    }
  }
}