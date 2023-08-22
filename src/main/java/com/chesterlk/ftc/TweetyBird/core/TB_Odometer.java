package com.chesterlk.ftc.tweetybird.core;

import com.chesterlk.ftc.tweetybird.util.TB_Config;
import com.qualcomm.robotcore.hardware.DcMotor;

public class TB_Odometer extends Thread {

    //Positions (These are stored in the original class, not the thread)
    protected double X = 0;
    protected double Y = 0;
    protected double Z = 0;

    //Storing variables here for quicker access (These do not need to be static because the class is copied with thread)
    final double Lx = TB_Config.radiusToLeftEncoder;
    final double Rx = TB_Config.radiusToRightEncoder+0.0001;
    final double By = TB_Config.radiusToBackEncoder;
    final double ticksPerInch = TB_Config.ticksPerInch;

    //The thread (When the thread is ran a duplicate is created, so to make things simple, the thread will reference this original class)
    @Override
    public void run() {
        //Clearing Values
        X = 0;
        Y = 0;
        Z = 0;

        //Waiting for Start
        TB_Master.opMode.waitForStart();

        //Resting Encoder Positions
        TB_Master.drivetrain.leftEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        TB_Master.drivetrain.rightEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        TB_Master.drivetrain.backEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Re-enabling Motors since they are linked to the actual motors
        TB_Master.drivetrain.leftEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        TB_Master.drivetrain.rightEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        TB_Master.drivetrain.backEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        //Storage
        int[] prevEncoderPos = {0,0,0}; //Used to see how far the robot moved

        //Loop until robot is stopped
        while (TB_Master.opMode.opModeIsActive()) {

            //Getting Encoder Positions
            int[] rawEncoderPos = {TB_Master.drivetrain.leftEncoder.getCurrentPosition(),
                    TB_Master.drivetrain.rightEncoder.getCurrentPosition(),
                    TB_Master.drivetrain.backEncoder.getCurrentPosition()};

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

    protected String dataToString() {
        return ("X ="+X+"\n" +
                "Y ="+Y+"\n" +
                "Z ="+Z);
    }

}
