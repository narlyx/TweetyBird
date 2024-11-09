package dev.narlyx.tweetybird.Odometers;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import dev.narlyx.tweetybird.TweetyBird;

/**
 * This is an example odometer for the common three wheel odometer setup.
 */
public class ThreeWheeled implements TweetyBird.Odometer {

  // Imported variables from builder
  private final DcMotor leftEncoder, rightEncoder, middleEncoder;
  private final boolean flipLeftEncoder, flipRightEncoder, flipMiddleEncoder;
  private final double sideEncoderDistance, middleEncoderOffset, inchesPerTick;

  /**
   * Constructor used to setup all variables from builder.
   * @param builder Passed builder
   */
  public ThreeWheeled(ThreeWheeled.Builder builder) {
    this.leftEncoder = builder.leftEncoder;
    this.rightEncoder = builder.rightEncoder;
    this.middleEncoder = builder.middleEncoder;
    this.flipLeftEncoder = builder.flipLeftEncoder;
    this.flipRightEncoder = builder.flipRightEncoder;
    this.flipMiddleEncoder = builder.flipMiddleEncoder;
    this.sideEncoderDistance = builder.sideEncoderDistance;
    this.middleEncoderOffset = builder.middleEncoderOffset;
    double encoderWheelRadius = builder.encoderWheelRadius;
    double encoderTicksPerRotation = builder.encoderTicksPerRotation;

    inchesPerTick = 2.0*Math.PI*(encoderWheelRadius / encoderTicksPerRotation);
  }

  // Cache
  private double X = 0;
  private double Y = 0;
  private double Z = 0;

  private double xOffset = 0;
  private double yOffset = 0;
  private double zOffset = 0;

  private int lastLeftPos = 0;
  private int lastRightPos = 0;
  private int lastMiddlePos = 0;

  /**
   * An internal method used to update all cached values before returning anything.
   */
  private void update() {
    int rawLeftPos = leftEncoder.getCurrentPosition()*(flipLeftEncoder?-1:1);
    int rawRightPos = rightEncoder.getCurrentPosition()*(flipRightEncoder?-1:1);
    int rawMiddlePos = middleEncoder.getCurrentPosition()*(flipMiddleEncoder?-1:1);

    int leftPos = (rawLeftPos)-lastLeftPos;
    int rightPos = (rawRightPos)-lastRightPos;
    int middlePos = (rawMiddlePos)-lastMiddlePos;

    lastLeftPos = rawLeftPos;
    lastRightPos = rawRightPos;
    lastMiddlePos = rawMiddlePos;

    double initialZ = inchesPerTick*((rightPos-leftPos)/sideEncoderDistance);
    double initialY = inchesPerTick*((leftPos+rightPos)/2.0);
    double initialX = inchesPerTick*(middlePos-(rightPos-leftPos)*(middleEncoderOffset/sideEncoderDistance));

    Z -= initialZ;
    double theta = Z+(initialZ/2.0);
    double relativeY = initialY*Math.cos(theta)-initialX*Math.sin(theta);
    double relativeX = initialY*Math.sin(theta)+initialX*Math.cos(theta);

    X += relativeX;
    Y += relativeY;
  }

  /**
   * Returns estimated X (lateral) coordinate.
   * @return Unit of measurement
   */
  @Override
  public double getX() {
    update();
    return X-xOffset;
  }

  /**
   * Returns estimated Y (axial) coordinate.
   * @return Unit of measurement
   */
  @Override
  public double getY() {
    update();
    return Y-yOffset;
  }

  /**
   * Returns estimated Z (yaw) coordinate.
   * @return Unit of measurement
   */
  @Override
  public double getZ() {
    update();
    return Z-zOffset;
  }

  /**
   * Will reset the estimated position based on imputed values.
   * @param x Lateral unit of measurement
   * @param y Axial unit of measurement
   * @param z Yaw unit of measurement
   */
  @Override
  public void resetTo(double x, double y, double z) {
    xOffset = getX()+xOffset-x;
    yOffset = getY()+yOffset-y;
    zOffset = getZ()+zOffset-z;
  }

  /**
   * Used to configure and start the odometer.
   */
  public static class Builder {
    private DcMotor leftEncoder = null;
    /**
     * REQUIRED
     * Defines the encoder on the left side of the robot.
     * @param leftEncoder DCMotor reference to the encoder
     * @return Updated builder
     */
    public Builder setLeftEncoder(DcMotor leftEncoder) {
      this.leftEncoder = leftEncoder;
      return this;
    }

    private boolean flipLeftEncoder = false;
    /**
     * OPTIONAL
     * Reverses the direction of the left encoder if counting backwards,
     * this encoder must be counting up when the bot is pushed forwards.
     * @param flipLeftEncoder True to flip
     * @return Updated builder
     */
    public Builder setFlipLeftEncoder(boolean flipLeftEncoder) {
      this.flipLeftEncoder = flipLeftEncoder;
      return this;
    }

    private DcMotor rightEncoder = null;
    /**
     * REQUIRED
     * Defines the encoder on the right side of the robot.
     * @param rightEncoder DCMotor reference to the encoder
     * @return Updated builder
     */
    public Builder setRightEncoder(DcMotor rightEncoder) {
      this.rightEncoder = rightEncoder;
      return this;
    }

    private boolean flipRightEncoder = false;
    /**
     * OPTIONAL
     * Reverses the direction of the right encoder if counting backwards,
     * this encoder must be counting up when the bot is pushed forwards.
     * @param flipRightEncoder True to flip
     * @return Updated builder
     */
    public Builder setFlipRightEncoder(boolean flipRightEncoder) {
      this.flipRightEncoder = flipRightEncoder;
      return this;
    }

    private DcMotor middleEncoder = null;
    /**
     * REQUIRED
     * Defines the encoder in the middle of the bot.
     * @param middleEncoder DCMotor reference to the encoder
     * @return Updated builder
     */
    public Builder setMiddleEncoder(DcMotor middleEncoder) {
      this.middleEncoder = middleEncoder;
      return this;
    }

    private boolean flipMiddleEncoder = false;
    /**
     * OPTIONAL
     * Reverses the direction of the middle encoder if counting backwards,
     * this encoder must be counting up when the bot is pushed to the right.
     * @param flipMiddleEncoder True to flip
     * @return Updated builder
     */
    public Builder setFlipMiddleEncoder(boolean flipMiddleEncoder) {
      this.flipMiddleEncoder = flipMiddleEncoder;
      return this;
    }

    private double sideEncoderDistance = 0;
    /**
     * REQUIRED TO FUNCTION PROPERLY
     * The distance between the left and right encoders.
     * @param sideEncoderDistance Unit of measurement
     * @return Updated builder
     */
    public Builder setSideEncoderDistance(double sideEncoderDistance) {
      this.sideEncoderDistance = sideEncoderDistance;
      return this;
    }

    private double middleEncoderOffset = 0;
    /**
     * REQUIRED TO FUNCTION PROPERLY
     * The distance from the center of rotation to the middle encoder,
     * if the encoder is to the front of the bot, then this value will be positive,
     * this the encoder is to the back of the bot, this value will be negative.
     * @param middleEncoderOffset Unit of measurement
     * @return Updated builder
     */
    public Builder setMiddleEncoderOffset(double middleEncoderOffset) {
      this.middleEncoderOffset = middleEncoderOffset;
      return this;
    }

    private double encoderWheelRadius = 0;
    /**
     * REQUIRED TO FUNCTION PROPERLY
     * The radius of the wheel attached to your encoder.
     * @param encoderWheelRadius Unit of measurement
     * @return Updated builder
     */
    public Builder setEncoderWheelRadius(double encoderWheelRadius) {
      this.encoderWheelRadius = encoderWheelRadius;
      return this;
    }

    private double encoderTicksPerRotation = 0;
    /**
     * REQUIRED TO FUNCTION PROPERLY
     * How many times your encoder will count during one full rotation (360 degrees),
     * you can typically find this information on the manufacturers website.
     * @param encoderTicksPerRotation Number of counts
     * @return Updated builder
     */
    public Builder setEncoderTicksPerRotation(double encoderTicksPerRotation) {
      this.encoderTicksPerRotation = encoderTicksPerRotation;
      return this;
    }

    /**
     * This will construct and return a new Odometer
     * @return ThreeWheeled Odometer
     */
    public ThreeWheeled build() {
      return new ThreeWheeled(this);
    }
  }

}
