package dev.narlyx.tweetybird.Drivers;

import com.qualcomm.robotcore.hardware.DcMotor;

import dev.narlyx.tweetybird.TweetyBird;

/**
 * A simple example driver for a Mecanum drivetrain setup.
 */
public class Mecanum implements TweetyBird.Driver {

  // Imported from builder
  private final DcMotor frontLeft, frontRight, backLeft, backRight;

  /**
   * Constructor used to setup all variables from builder.
   * @param builder Passed builder
   */
  public Mecanum(Mecanum.Builder builder) {
    this.frontLeft = builder.frontLeft;
    this.frontRight = builder.frontRight;
    this.backLeft = builder.backLeft;
    this.backRight = builder.backRight;
  }

  /**
   * This method will power all four motors based on a target Axial, Lateral, Yaw, and Speed input
   * @param axial Value from -1 to 1 to favor the axial direction
   * @param lateral Value from -1 to 1 to favor the lateral direction
   * @param yaw Value from -1 to 1 to set rotation
   * @param speed Value from 0 to 1 to set how fast the bot will cary out axial and lateral
   */
  @Override
  public void setHeading(double axial, double lateral, double yaw, double speed) {
    // Fetching values
    double frontLeftPower  = ((axial + lateral) * speed) + (yaw);
    double frontRightPower = ((axial - lateral) * speed) - (yaw);
    double backLeftPower   = ((axial - lateral) * speed) + (yaw);
    double backRightPower  = ((axial + lateral) * speed) - (yaw);

    // Powering motors
    frontLeft.setPower(frontLeftPower);
    frontRight.setPower(frontRightPower);
    backLeft.setPower(backLeftPower);
    backRight.setPower(backRightPower);

    // Setting correct mode
    if (frontLeft.getZeroPowerBehavior()== DcMotor.ZeroPowerBehavior.BRAKE) {
      frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }
    if (frontRight.getZeroPowerBehavior()== DcMotor.ZeroPowerBehavior.BRAKE) {
      frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }
    if (backLeft.getZeroPowerBehavior()== DcMotor.ZeroPowerBehavior.BRAKE) {
      backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }
    if (backRight.getZeroPowerBehavior()== DcMotor.ZeroPowerBehavior.BRAKE) {
      backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }
  }

  /**
   * This method will stop all of the motors and attempt to lock them in place
   */
  @Override
  public void stopAndHold() {
    // Stopping motors
    frontLeft.setPower(0);
    frontRight.setPower(0);
    backLeft.setPower(0);
    backRight.setPower(0);

    // Holding
    if (frontLeft.getZeroPowerBehavior()== DcMotor.ZeroPowerBehavior.FLOAT) {
      frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }
    if (frontRight.getZeroPowerBehavior()== DcMotor.ZeroPowerBehavior.FLOAT) {
      frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }
    if (backLeft.getZeroPowerBehavior()== DcMotor.ZeroPowerBehavior.FLOAT) {
      backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }
    if (backRight.getZeroPowerBehavior()== DcMotor.ZeroPowerBehavior.FLOAT) {
      backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }
  }

  /**
   * Used to configure and start the driver.
   */
  public static class Builder {
    private DcMotor frontLeft = null;
    /**
     * REQUIRED
     * Defines the motor on the front left of the robot.
     * @param frontLeft DCMotor reference
     * @return Updated builder
     */
    public Builder setFrontLeftMotor(DcMotor frontLeft) {
      this.frontLeft = frontLeft;
      return this;
    }

    private DcMotor frontRight = null;
    /**
     * REQUIRED
     * Defines the motor on the front right of the robot.
     * @param frontRight DCMotor reference
     * @return Updated builder
     */
    public Builder setFrontRightMotor(DcMotor frontRight) {
      this.frontRight = frontRight;
      return this;
    }

    private DcMotor backLeft = null;
    /**
     * REQUIRED
     * Defines the motor on the back left of the robot.
     * @param backLeft DCMotor reference
     * @return Updated builder
     */
    public Builder setBackLeftMotor(DcMotor backLeft) {
      this.backLeft = backLeft;
      return this;
    }

    private DcMotor backRight = null;
    /**
     * REQUIRED
     * Defines the motor on the back right of the robot.
     * @param backRight DCMotor reference
     * @return Updated builder
     */
    public Builder setBackRightMotor(DcMotor backRight) {
      this.backRight = backRight;
      return this;
    }

    /**
     * This will construct and return a new Odometer
     * @return ThreeWheeled Odometer
     */
    public Mecanum build() {
      return new Mecanum(this);
    }
  }
}
