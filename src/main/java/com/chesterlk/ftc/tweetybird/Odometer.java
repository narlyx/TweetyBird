package com.chesterlk.ftc.tweetybird;

import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Odometer class, calculates where the robot is currently at
 */
public class Odometer extends Thread {

    //Processor
    private TweetyBirdProcessor processor;

    //Status
    /**
     * Variable determining weather the class is running or not
     */
    protected boolean running = false;
    /**
     * Variable determining if the class should stop or not
     */
    private boolean stopRequested = false;

    //Positions (These are stored in the original class, not the thread)
    protected double X = 0.0001;
    protected double Y = 0.0001;
    protected double Z = 0.0001;

    protected double Xoffset = 0;
    protected double Yoffset = 0;
    protected double Zoffset = 0;

    //Storing variables here for quicker access (These do not need to be static because the class is copied with thread)
    double L;
    double B;
    double ticksPerInch;
    double inchsPerTick;

    public Odometer(TweetyBirdProcessor processor) {
        //Status
        running = false;
        stopRequested = false;

        //Setting Processor
        this.processor = processor;
    }

    //The thread (When the thread is ran a duplicate is created, so to make things simple, the thread will reference this original class)
    @Override
    public void run() {
        //Setting
        X = 0;
        Y = 0;
        Z = 0;

        L = processor.inchesBetweenSideEncoders;
        B = processor.inchesToBackEncoder;
        ticksPerInch = processor.ticksPerInch;
        inchsPerTick = processor.inchesPerTick;

        //Updating status
        stopRequested = false;
        this.running = true;

        //Waiting for Start
        processor.opMode.waitForStart();

        //Resting Encoder Positions
        processor.le.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        processor.re.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        processor.me.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Re-enabling Motors since they are linked to the actual motors
        processor.le.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        processor.re.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        processor.me.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        //Storage
        int[] prevEncoderPos = {0,0,0}; //Used to see how far the robot moved

        //Loop until robot is stopped

        while (processor.opMode.opModeIsActive()||!stopRequested) {

            //Getting Encoder Positions
            int[] rawEncoderPos = {processor.le.getCurrentPosition()*(processor.flipLe?-1:1),
                    processor.re.getCurrentPosition()*(processor.flipRe?-1:1),
                    processor.me.getCurrentPosition()*(processor.flipMe?-1:1)};

            //Getting the Amount Each Encoder Moved Since the Last Cycle
            int[] movedPositions = {rawEncoderPos[0]-prevEncoderPos[0],
                    rawEncoderPos[1]-prevEncoderPos[1],
                    rawEncoderPos[2]-prevEncoderPos[2]};

            //Saving Current Position for Next Run
            prevEncoderPos = rawEncoderPos;

            //Change value names for simplicity
            double LE = movedPositions[0];
            double RE = movedPositions[1];
            double BE = movedPositions[2];

            //General Positioning
            double preZ = inchsPerTick*((RE-LE)/L);
            double preY = inchsPerTick*((LE+RE)/2.0);
            double preX = inchsPerTick*(BE-(RE-LE)*(B/L));

            //Relative Positioning
            Z += preZ-Zoffset;
            double theta = Z+(preZ/2.0);
            double relY = preY*Math.cos(theta)-preX*Math.sin(theta);
            double relX = preY*Math.sin(theta)+preX*Math.cos(theta);

            //Setting Values
            X -= relX-Xoffset;
            Y -= relY-Yoffset;

        }
    }

    //Stop
    protected void requestStop() {
        stopRequested = true;
    }

}
