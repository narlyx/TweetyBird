package dev.narlyx.tweetybird;

import android.os.Environment;

import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.Range;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

/**
 * This is the main class behind TweetyBird, use this class to setup, start, and use TweetyBird.
 */
public class TweetyBird {

  private static final Logger log = LoggerFactory.getLogger(TweetyBird.class);

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
  protected final double distanceBuffer, rotationBuffer;
  protected double minSpeed, maxSpeed;
  protected final double speedModifier, correctionOverpower;
  protected final boolean debuggingEnabled, loggingEnabled;

  // Other classes used by TweetyBird
  protected final Runtime runtime;

  // Log file writer
  protected BufferedWriter logWriter = null;

  /**
   * This method has been renamed, please use addWaypoint(x, y, z) instead
   * @param x Target X
   * @param y Target Y
   * @param z Target Z
   */
  @Deprecated(since = "0.2.0", forRemoval = false)
  public void sendTargetPosition(double x, double y, double z) {
    addWaypoint(x, y, z);
  }

  /**
   * Creates a new waypoint and adds it to the end of TweetyBird's queue
   * @param x Target X
   * @param y Target Y
   * @param z Target Z
   */
  public void addWaypoint(double x, double y, double z) {
    waypointQueue.addWaypoint(new Waypoint(x, y, Math.toRadians(z)));
  }

  /**
   * Adds a waypoint that will bypass the queue and be run imediently
   * @param x Target X
   * @param y Target Y
   * @param z Target Z
   */
  public void injectWaypoint(double x, double y, double z) {
    waypointQueue.addWaypoint(waypointQueue.getIndex(), new Waypoint(x, y, Math.toRadians(z)));
  }

  /**
   * Skips the current targeted waypoint
   */
  public void skipWaypoint() {
    if (waypointQueue.getIndex() == waypointQueue.getSize() -1) { // Resetting at end of queue
      waypointQueue.clear();
    } else { // Skip next waypoint
      waypointQueue.increment();
    }
  }

  /**
   * Clears all waypoints in queue
   */
  public void clearWaypoints() {
    waypointQueue.clear();
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
   * Readjusts the maximum speed variable
   * @param speed New maximum speed
   */
  public void setMaxSpeed(double speed) {
    maxSpeed = Range.clip(speed, minSpeed, 1);
  }

  /**
   * Readjusts the minimum speed variable
   * @param speed New minimum speed
   */
  public void setMinSpeed(double speed) {
    minSpeed = Range.clip(speed, 0, maxSpeed);
  }

  /**
   * Returns whether the mover is currently in progress or not
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
      opMode.sleep(100);
      while (isBusy() && opMode.opModeIsActive());
    } else {
      try {
        wait(100);
      } catch (InterruptedException e) {}
      while (isBusy());
    }
  }

  /**
   * Terminates TweetyBird
   */
  public void close() {
    log("TweetyBird close called, shutting down...");
    try {
      if (logWriter != null) {
        logWriter.flush();
        logWriter.close();
      }
    } catch (IOException e) {
      log("Failed to shutdown logWriter");
    }
    runtime.interrupt();
  }

  /**
   * Internal method used to send debug messages
   * @param message message to be sent
   */
  protected void log(String message) {
    // Getting current time
    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/YYYY hh:mm:ss.SSS");
    String date = sdf.format(now);

    // Processing string
    String outputString = "["+date+" TweetyBird]: "+message;

    // Logfile
    if (loggingEnabled && logWriter != null) {
      try {
        logWriter.write(outputString);
        logWriter.newLine();
      } catch (IOException e) {}
    }

    // Console
    if (debuggingEnabled) {
      if (opMode == null) { // Normal
        System.out.println(outputString);
      } else { // FTC
        opMode.telemetry.addLine(outputString);
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
    this.speedModifier = builder.speedModifier;
    this.correctionOverpower = 5;
    this.distanceBuffer = builder.distanceBuffer;
    this.rotationBuffer = Math.toRadians(builder.rotationBuffer);
    this.debuggingEnabled = builder.debuggingEnabled;
    this.loggingEnabled = builder.loggingEnabled;

    // Setting up queue
    waypointQueue = new WaypointQueue(this);

    // Starting runtime
    runtime = new Runtime(this);
    runtime.start();

    // Setting up log file
    String logFileName = "tweetyBirdLog.txt";
    File logFile;
    if (opMode != null) {
      File logDirectory = Environment.getExternalStorageDirectory();
      logFile = new File(logDirectory, logFileName);
    } else {
      logFile = new File(logFileName);
    }
    try {
      logWriter = new BufferedWriter(new FileWriter(logFile, true));
    } catch (IOException e) {
      log("Failed to initialize to logWriter "+e);
    }

    // Done
    log("Initial setup complete!\n");
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

    public double speedModifier = 0.05;

    /**
     * OPTIONAL
     * This value is used in calculations for acceleration and deceleration internally to determine
     * when to reach full speed and when to slow down (distance*speedModifier=maxSpeed),
     * the default value is 0.05.
     * @param speedModifier New speed modifier
     * @return Updated builder
     */
    public Builder setSpeedModifier(double speedModifier) {
      this.speedModifier = speedModifier;
      return this;
    }

    private boolean debuggingEnabled = false;

    /**
     * NOT RECOMMENDED
     * This wil flood your console with debug messages, only use for development
     * and will slow the runtime loop without a linear op mode,
     * the default value is false
     * @param debuggingEnabled Whether to enable debug logs
     * @return Updated builder
     */
    public Builder setDebuggingEnabled(boolean debuggingEnabled) {
      this.debuggingEnabled = debuggingEnabled;
      return this;
    }

    private boolean loggingEnabled = false;

    /**
     * OPTIONAL
     * This will print all console and debug logs from TweetyBird to a file
     * @param loggingEnabled Whether to enable debug logs
     * @return Updated builder
     */
    public Builder setLoggingEnabled(boolean loggingEnabled) {
      this.loggingEnabled = loggingEnabled;
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