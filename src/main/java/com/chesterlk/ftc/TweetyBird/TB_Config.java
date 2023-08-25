package com.chesterlk.ftc.tweetybird;

public class TB_Config {
    //Physical Robot Measurements
        //Dead Wheel Locations
        public static final double radiusToLeftEncoder = 7.625; //Distance from center of rotation to the left encoder in inches
        public static final double radiusToRightEncoder = 7.625; //Distance from center of rotation to the right encoder in inches
        //public static final double sideEncoderOffset = 0.1875; //Offset (+forward -backward) off the center of the side in inches
        public static final double radiusToBackEncoder = 7; //Distance from center of rotation to back encoder in inches

        //Dead Wheels
        public static final int ticksPerEncoderRotation = 8192; //Amount of counts per encoder rotation
        public static final double encoderWheelRadius = 1; //Size from center of wheel to outer edge of wheel in inches
        public static final double ticksPerInch = (double)ticksPerEncoderRotation/((double)2*Math.PI*encoderWheelRadius); //!!Created automatically, no touch!!



    //TB_Mover
    public static final double minSpeed = 0.13;
    public static final double maxSpeed = 0.5;
    public static final double startBoostSpeed = 0.17; //Similar to min speed but only used to launch the robot from a stop.

    public static final double speedModifier = 0.06; //If your robot is launching too fast, decrease, if it is launching too slow, increase, only change by 0.01 (sensitive)

    public static final double stopForceSpeed = 0.1; //When your robot is at a stand still, how much power will be applied to keep robot in place, (too high results in skidding)
    public static final double correctionOverpowerDistance = 5; //How far the robot needs to be off course before it forgets the target and fixes itself (in inches)
}
