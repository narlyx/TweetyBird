package dev.narlyx.ftc.tweetybird;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

public class TweetyBirdProcessor {

    /**
     * Status Classes
     */
    protected boolean running = false;

    /**
     * Parameters and Configuration
     */
    protected LinearOpMode opMode;

    protected DcMotor fl;
    protected DcMotor fr;
    protected DcMotor bl;
    protected DcMotor br;
    protected DcMotor le;
    protected DcMotor re;
    protected DcMotor me;

    protected boolean flipLe;
    protected boolean flipRe;
    protected boolean flipMe;

    protected double sideEncoderDistance;
    protected double middleEncoderOffset;

    protected int ticksPerEncoderRotation;
    protected double encoderWheelRadius;
    protected double ticksPerInch;
    protected double inchesPerTick;

    protected double minSpeed;
    protected double maxSpeed;
    protected double startSpeed;
    protected double stopForceSpeed;
    protected double speedModifier;

    protected double correctionOverpowerDistance;
    protected double rotationBuffer;
    protected double distanceBuffer;

    private boolean inputFlipped = false;

    /**
     * Classes
     */
    Odometer odometer;
    Queue queue;
    Mover mover;


    /**
     * Creates and follows a straight linear line to the specified destination relative to the starting point
     * @param x destination coordinate in inches
     * @param y destination coordinate in inches
     * @param z destination coordinate in inches
     */
    public void straightLineTo(double x, double y, double z) {
        double modifier = inputFlipped?-1.0:1.0;
        queue.add(new Waypoint(x*modifier,y,Math.toRadians(z)*modifier));
        while (opMode.opModeIsActive()&&!queue.waypointReceived);
        queue.waypointReceived = false;
    }

    /**
     * Creates a new line that is adjusted based from the previous input
     * @param x adjustment in inches
     * @param y adjustment in inches
     * @param z adjustment in inches
     */
    public void adjustTo(double x, double y, double z) {
        double modifier = inputFlipped?-1.0:1.0;
        double currX = getX();
        double currY = getY();
        double currZ = getZ();
        queue.add(new Waypoint(currX+x*modifier,currY+y,currZ+Math.toRadians(z)*modifier));
        while (opMode.opModeIsActive()&&!queue.waypointReceived);
        queue.waypointReceived = false;
    }

    /**
     * Sets the speed limit
     * @param speed new speedlimit
     */
    public void speedLimit(double speed) {
        maxSpeed = speed;
    }

    /**
     * Will set the current position as 0,0,0
     */
    public void resetPosition() {
        odometer.Xoffset = odometer.X;
        odometer.Yoffset = odometer.Y;
        odometer.Zoffset = odometer.Z;
    }

    /**
     * Resets position to the input'd values
     * @param X new X
     * @param Y new Y
     * @param Z new Z
     */
    public void resetTo(double X, double Y, double Z) {
        odometer.Xoffset = odometer.X-X;
        odometer.Yoffset = odometer.Y-Y;
        odometer.Zoffset = odometer.Z-Z;
    }

    /**
     * Stop TweetyBird from controlling the drivetrain
     */
    public void disengage() {
        mover.engaged = false;
    }

    /**
     * Allows TweetyBird to control the drivetrain (Automatically called)
     */
    public void engage() {
        mover.engaged = true;
        mover.busy = true;
    }

    /**
     * @return true if TweetyBird is currently busy (moving)
     */
    public boolean busy() {
        return mover.busy;
    }

    /**
     * @return true if TweetyBird is currently engaged (controlling robot)
     */
    public boolean engaged() {
        return mover.engaged;
    }

    /**
     * Saves lines of code by waiting or holding up code until TweetyBird is no longer busy
     * (or the opmode stops)
     */
    public void waitWhileBusy() {
        mover.busy=true;
        while (opMode.opModeIsActive()&&busy());
        opMode.telemetry.addData("Variables",busy()+" "+opMode.opModeIsActive());
    }

    /**
     * Will delete every waypoint (movement points) in queue and stop
     * Meant to be used for when you need to "cancel" a movement
     */
    public void clearQueue() {
        disengage();
        mover.movementPower(0,0,0,0);
        queue.clear();
        while (opMode.opModeIsActive()&&!queue.waypointReceived);
        queue.waypointReceived = false;
        engage();
        mover.cycleWaypoint();
    }

    /**
     * If true any inputs made for positioning will be negative of what you input
     * @param flip true for flip
     */
    public void flipInput(boolean flip) {
        inputFlipped = flip;
    }


    /**
     * Builds TweetyBird via the builder method and gets the rest of the classes ready to start
     * @param builder Takes in parameters from the builder class
     */
    public TweetyBirdProcessor(Builder builder) {
        /**
         * Status Update
         */

        this.running = false;

        /**
         * Migrating and created all parameters and saving them in the current class
         */
        this.opMode = builder.opMode;

        this.fl = builder.fl;
        this.fr = builder.fr;
        this.bl = builder.bl;
        this.br = builder.br;
        this.le = builder.le;
        this.re = builder.re;
        this.me = builder.be;

        this.flipLe = builder.flipLe;
        this.flipRe = builder.flipRe;
        this.flipMe = builder.flipMe;

        this.middleEncoderOffset = builder.middleEncoderOffset;
        this.sideEncoderDistance = builder.sideEncoderDistance;

        this.ticksPerEncoderRotation = builder.ticksPerEncoderRotation;
        this.encoderWheelRadius = builder.encoderWheelRadius;
        this.ticksPerInch = (double)this.ticksPerEncoderRotation/((double)2*Math.PI*this.encoderWheelRadius);
        this.inchesPerTick = 2.0*Math.PI*(this.encoderWheelRadius/ticksPerEncoderRotation);

        this.minSpeed = builder.minSpeed;
        this.maxSpeed = builder.maxSpeed;
        this.startSpeed = builder.startSpeed;
        this.stopForceSpeed = builder.stopForceSpeed;
        this.speedModifier = builder.speedModifier;

        this.correctionOverpowerDistance = builder.correctionOverpowerDistance;
        this.rotationBuffer = builder.rotationBuffer;
        this.distanceBuffer = builder.distanceBuffer;

        /**
         * Starting Classes
         */
        //Odometer
        this.odometer = new Odometer(this);
        this.odometer.start();
        while (!odometer.running) {
            if (!opMode.opModeIsActive()) {
                break;
            }
        }

        //Queue
        this.queue = new Queue(this);
        this.queue.clear();
        while (!queue.running) {
            if (!opMode.opModeIsActive()) {
                break;
            }
        }

        //Mover
        this.mover = new Mover(this);
        this.mover.start();
        while (!mover.running) {
            if (!opMode.opModeIsActive()) {
                break;
            }
        }
        engage();

        /**
         * Status Update
         */
        running = true;
    }

    /**
     * @return X Position
     */
    public double getX() {
        return odometer.X;
    }

    /**
     * @return Y Position
     */
    public double getY() {
        return odometer.Y;
    }

    /**
     * @return Z Position
     */
    public double getZ() {
        return odometer.Z;
    }

    /**
     * Stop Classes
     */
    public void stop() {
        mover.requestStop();
        odometer.requestStop();
        while (mover.running&& odometer.running&&opMode.opModeIsActive());
        mover = null;
        running = false;
    }


    /**
     * Used to specify all parameters before starting TweetyBird *required
     */
    public static class Builder {
        /**
         * Opmode
         */

        private LinearOpMode opMode = null;

        /**
         * The Linear Opmode Instance, if you don't know what this means, chances are
         * you can just type .setOpMode(this) for this parameter.
         * @param opMode
         */
        public Builder setOpMode(LinearOpMode opMode) {
            this.opMode = opMode;
            return this;
        }

        /**
         * Hardware Parameters
         */

        private DcMotor fl = null;
        private DcMotor fr = null;
        private DcMotor bl = null;
        private DcMotor br = null;
        private DcMotor le = null;
        private DcMotor re = null;
        private DcMotor be = null;

        private boolean flipLe = false;
        private boolean flipRe = false;
        private boolean flipMe = false;

        /**
         * The instance/variable of the front left motor.
         * @param dcMotor DC Motor instance
         */
        public Builder setFrontLeftMotor(DcMotor dcMotor) {
            this.fl = dcMotor;
            return this;
        }
        /**
         * The instance/variable of the front right motor.
         * @param dcMotor DC Motor instance
         */
        public Builder setFrontRightMotor(DcMotor dcMotor) {
            this.fr = dcMotor;
            return this;
        }
        /**
         * The instance/variable of the back left motor.
         * @param dcMotor DC Motor instance
         */
        public Builder setBackLeftMotor(DcMotor dcMotor) {
            this.bl = dcMotor;
            return this;
        }
        /**
         * The instance/variable of the back right motor.
         * @param dcMotor DC Motor instance
         */
        public Builder setBackRightMotor(DcMotor dcMotor) {
            this.br = dcMotor;
            return this;
        }
        /**
         * The instance/variable of the left encoder "motor".
         * @param dcMotor DC Motor instance
         */
        public Builder setLeftEncoder(DcMotor dcMotor) {
            this.le = dcMotor;
            return this;
        }
        /**
         * The instance/variable of the right encoder "motor".
         * @param dcMotor DC Motor instance
         */
        public Builder setRightEncoder(DcMotor dcMotor) {
            this.re = dcMotor;
            return this;
        }
        /**
         * The instance/variable of the middle/back/front encoder "motor".
         * @param dcMotor DC Motor instance
         */
        public Builder setMiddleEncoder(DcMotor dcMotor) {
            this.be = dcMotor;
            return this;
        }

        /**
         * Flips the input of the Left encoder
         * @param flip yay/nae?
         */
        public Builder flipLeftEncoder(Boolean flip) {
            this.flipLe = flip;
            return this;
        }

        /**
         * Flips the input of the Right encoder
         * @param flip yay/nae?
         */
        public Builder flipRightEncoder(Boolean flip) {
            this.flipRe = flip;
            return this;
        }

        /**
         * Flips the input of the Middle encoder
         * @param flip yay/nae?
         */
        public Builder flipMiddleEncoder(Boolean flip) {
            this.flipMe = flip;
            return this;
        }


        /**
         * Encoder Position Parameters
         */
        double sideEncoderDistance = 0;
        double middleEncoderOffset = 0;

        /**
         * Distance from the robot's center of rotation to the center of the left encoder wheel.
         * Only 2d, height is not accounted.
         *
         * @param inches
         */
        public Builder setSideEncoderDistance(double inches) {
            this.sideEncoderDistance = inches;
            return this;
        }

        /**
         * Distance from the robot's center of rotation to the middle encoder on the robots Y-axis
         * (How far forward or backwards the middle encoder is)
         *
         * @param inches
         */
        public Builder setMiddleEncoderOffset(double inches) {
            this.middleEncoderOffset = inches;
            return this;
        }

        /**
         * Encoder Wheel Parameters
         */
        private int ticksPerEncoderRotation = 0;
        private double encoderWheelRadius = 0;

        /**
         * How many ticks will be counted for a full 360 of the encoder
         * @param ticks ticks counted by encoder
         */
        public Builder setTicksPerEncoderRotation(int ticks) {
            this.ticksPerEncoderRotation = ticks;
            return this;
        }

        /**
         * Radius of the wheel attached to the encoder
         * @param radius radius of encoder wheel
         */
        public Builder setEncoderWheelRadius(double radius) {
            this.encoderWheelRadius = radius;
            return this;
        }

        /**
         * TweetyBird Speed Parameters
         */

        private double minSpeed = 0;
        private double maxSpeed = 1;
        private double startSpeed = 0.1;
        private double stopForceSpeed = 0;
        private double speedModifier = 0.06;

        /**
         * The default "speed limit", but for how slow the robot can go. Usually in affect
         * when slowing for corners or the destination.
         *
         * @param speed speed value 0-1
         */
        public Builder setMinSpeed(double speed) {
            this.minSpeed = Range.clip(speed,0,1);
            return this;
        }

        /**
         * The default "speed limit", the robot should never go higher than this speed.
         *
         * @param speed speed value 0-1
         */
        public Builder setMaxSpeed(double speed) {
            this.maxSpeed = Range.clip(speed,0,1);
            return this;
        }

        /**
         * Similar to minimum speed but is only used when the robot is at a stand still, it is good
         * to make this value higher than the minimum speed to have a little boost.
         *
         * @param speed speed value 0-1
         */
        public Builder setStartSpeed(double speed) {
            this.startSpeed = Range.clip(speed,0,1);
            return this;
        }

        /**
         * When the robot is at a stand still, how much force will be applied to each motor
         * to attempt to maintain its position, to low will mean no force, too high will skid.
         *
         * When set to zero, TweetyBird will simply apply a zero hold break.
         *
         * @param speed speed value 0-1
         */
        public Builder setStopForceSpeed(double speed) {
            this.stopForceSpeed = Range.clip(speed,0,1);
            return this;
        }

        /**
         * Used for internal calculations for speed. Speed is found by multiplying the distance
         * by the speed modifier. For example, if the distance from the target is five inches,
         * and the speed modifier is 0.05, the resulting speed (minus other calculations) will be
         * 0.25, or a quarter of the max speed.
         * Only adjust by 0.01 of an inch.
         *
         * @param modifier above^
         */
        public Builder setSpeedModifier(double modifier) {
            this.speedModifier = modifier;
            return this;
        }

        /**
         * TweetyBird Correction Parameters
         */

        private double correctionOverpowerDistance = 5;
        private double rotationBuffer = 0;
        private double distanceBuffer = 0;

        /**
         * The distance the robot needs to be off of the projected target path
         * before dedicating all its power to get back on track
         *
         * @param distance distance in inches
         */
        public Builder setCorrectionOverpowerDistance(double distance) {
            this.correctionOverpowerDistance = distance;
            return this;
        }

        /**
         * Sets the amount of degrees off the heading can be off of the target heading
         * and still state that the heading is correct.
         * Setting this value to small will cause the robot to indefinitely try to correct the heading
         *
         * @param degrees amount of degrees
         */
        public Builder setRotationBuffer(double degrees) {
            this.rotationBuffer = Math.toRadians(degrees);
            return this;
        }

        /**
         * Sets the distance the robot can be away from the target distance
         * and still state that the heading is correct.
         * Setting this value too small will cause the robot to indefinitely wiggle to try and
         * reach the target position.
         *
         * @param distance distance in inches
         */
        public Builder setDistanceBuffer(double distance) {
            this.distanceBuffer = distance;
            return this;
        }



        public TweetyBirdProcessor build() {
            return new TweetyBirdProcessor(this);
        }
    }
}
