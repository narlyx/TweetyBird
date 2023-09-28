package com.chesterlk.tweetybird;

import com.qualcomm.robotcore.hardware.DcMotor;

public class Odometer extends Thread {

    //Processor
    private TweetyBirdProcessor processor;

    //Status
    protected boolean running = false;
    private boolean stopRequested = false;

    //Positions (These are stored in the original class, not the thread)
    protected double X = 0;
    protected double Y = 0;
    protected double Z = 0;

    //Storing variables here for quicker access (These do not need to be static because the class is copied with thread)
    double Lx;
    double Rx;
    double By;
    double ticksPerInch;

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

        Lx = processor.radiusToLeftEncoder;
        Rx = processor.radiusToRightEncoder+0.0001;
        By = processor.radiusToBackEncoder;
        ticksPerInch = processor.ticksPerInch;

        //Updating status
        stopRequested = false;
        this.running = true;

        //Waiting for Start
        processor.opMode.waitForStart();

        //Resting Encoder Positions
        processor.le.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        processor.re.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        processor.be.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Re-enabling Motors since they are linked to the actual motors
        processor.le.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        processor.re.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        processor.be.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        //Storage
        int[] prevEncoderPos = {0,0,0}; //Used to see how far the robot moved

        //Loop until robot is stopped

        while (processor.opMode.opModeIsActive()||!stopRequested) {

            //Getting Encoder Positions
            int[] rawEncoderPos = {processor.le.getCurrentPosition(),
                    processor.re.getCurrentPosition(),
                    processor.be.getCurrentPosition()};

            //Getting the Amount Each Encoder Moved Since the Last Cycle
            int[] movedPositions = {rawEncoderPos[0]-prevEncoderPos[0],
                    rawEncoderPos[1]-prevEncoderPos[1],
                    rawEncoderPos[2]-prevEncoderPos[2]};

            //Saving Current Position for Next Run
            prevEncoderPos = rawEncoderPos;

            //Change value names for simplicity whilst converting to ticks to inches
            double L = movedPositions[0]/ticksPerInch;
            double R = movedPositions[1]/ticksPerInch;
            double B = movedPositions[2]/ticksPerInch;

            //Getting Directions Moved (not counting rotation)
            double relAxial = ( (L*Rx+R*Lx) / (Lx-Rx) ) / -152500;
            double theta = ( (R-L) / (Lx-Rx) ) / -152500;
            double relLateral = (B-By*theta);

            //Updating Theta as Static First for the Next Equation
            Z = Z-theta;

            //Finally getting the actual axial and lateral
            double axial = relAxial*Math.cos(Z)-relLateral*Math.sin(Z);
            double lateral = relLateral*Math.cos(Z)+relAxial*Math.sin(Z);

            //Updating the Rest of the Values as Static
            X = X+lateral;
            Y = Y+axial;

        }
    }

    //Stop
    protected void requestStop() {
        stopRequested = true;
    }

}
