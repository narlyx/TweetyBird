package com.chesterlk.ftc.tweetybird;

import static java.lang.Double.isNaN;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

public class TB_Mover extends Thread {

    //States
    protected static boolean busy = false;
    protected static boolean engaged = true;

    //Data
    private static double
    lastX=0.0001, lastY=0.0001, lastZ=0,
    targetX =0, targetY =0, targetZ =0,
    nextX =0, nextY =0, nextZ =0,
    projectionSlope=0, projectionIntersect=0, projectionHeading=0, //<< No it may not be final
    bisectorSlope=0, bisectorIntersect=0, bisectionX=0, bisectionY=0,
    targetHeading=0,
    correctionHeading=0, correctionPower=0,
    finalHeading=0, axial=0, lateral=0,
    speed=0,
    targetYaw=0,yawPower=0,yawOff=0,
    nextAngle=0

        ;


    //Thread
    @Override
    public void run() {
        //Wait for start
        TB_Master.opMode.waitForStart();

        //Runtime Loop
        while (TB_Master.opMode.opModeIsActive()) {

            //Updating Values such as the position
            if (updateCondition()) {
                updateCoreValues();
            }

            //Action
            if (moveCondition()) { //Move Robo
                smartMove();
                TB_Mover.busy = true;

            } else if (lockCondition()) { //Lock Robo
                lockDrivetrain();
                TB_Mover.busy = false;

            } else { //Relax Robo
                //unlockDrivetrain();
                TB_Mover.busy = false;
            }

            TB_Master.opMode.telemetry.addLine(dataToString());
            TB_Master.opMode.telemetry.update();

            //Getting next waypoint if needed and setting waypoint values
            if (cycleCondition()) {
                cycleWaypoint();
            }

        }
        clearAll();
        TB_Master.classes.readyToStop = true;
    }

    //Conditions
    private boolean cycleCondition() { //Returns weather or not the robot needs to continue to the next waypoint
        return TB_Master.classes.queue.getDistanceToCurrent()<1&driveCondition()&rotateCondition();
    }

    private boolean moveCondition() { //Determines weather the robot should move
        return TB_Mover.engaged&!lockCondition();
    }

    private boolean driveCondition() {
        return distanceForm(TB_Mover.targetX,TB_Mover.targetY, TB_Master.classes.odometer.X,TB_Master.classes.odometer.Y)<=0.5;
    }

    private boolean rotateCondition() {
        return yawOff<0.08;
    }

    private boolean lockCondition() { //Determines if the robot is in the correct spot
        return TB_Mover.engaged&driveCondition()&rotateCondition();
    }

    private boolean updateCondition() {
        return TB_Mover.busy;
    }



    //Bulk Update
    private void cycleWaypoint() { //Cycles values to allow the robot to continue
        TB_Master.classes.queue.increment();
        setLast(TB_Master.classes.queue.last().getX(),TB_Master.classes.queue.last().getY(),TB_Master.classes.queue.last().getZ());
        setTarget(TB_Master.classes.queue.current().getX(),TB_Master.classes.queue.current().getY(),TB_Master.classes.queue.current().getZ());
        setNext(TB_Master.classes.queue.next().getX(),TB_Master.classes.queue.next().getY(),TB_Master.classes.queue.next().getZ());
        updateProjection();
        updateNextAngle();
    }

    private void updateCoreValues() { //Updates the values needed during every loop
        updateBisector();
        updateTarget();
        updateCorrection();
        updateFinal();
        updateSpeed();
        updateTargetYaw();
        updateYawPower();
    }


    //Update Data
    private void setLast(double x, double y, double z) { //Last Waypoint
        TB_Mover.lastX = x;
        TB_Mover.lastY = y;
        TB_Mover.lastZ = z;
    }

    private void setTarget(double x, double y, double z) { //Target Waypoint
        TB_Mover.targetX = x==TB_Mover.lastX?x+0.0001:x;
        TB_Mover.targetY = y==TB_Mover.lastY?y+0.0001:y;
        TB_Mover.targetZ = z;
    }

    private void setNext(double x, double y, double z) {
        TB_Mover.nextX = x==TB_Mover.lastX?x+0.0001:x;
        TB_Mover.nextY = y==TB_Mover.lastY?y+0.0001:y;
        TB_Mover.nextZ = z;
    }

    private void updateNextAngle() {
        double a = distanceForm(targetX,targetY,nextX,nextY);
        double b = distanceForm(lastX,lastY,targetX,targetY);
        double c = distanceForm(lastX,lastY,nextX,nextY)+0.0001;

        TB_Mover.nextAngle = Math.toRadians(Math.acos((Math.pow(a,2)+Math.pow(b,1)-Math.pow(c,1))/(2*a*b)));

        if(isNaN(TB_Mover.nextAngle)) {
            TB_Mover.nextAngle = 0;
        }
    }

    private void updateProjection() { //Creates values for a line from last to target
        TB_Mover.projectionSlope = (targetY -lastY)/(targetX -lastX);
        TB_Mover.projectionIntersect = lastY-(TB_Mover.projectionSlope*lastX);
        TB_Mover.correctionHeading = Math.atan2(TB_Mover.targetX -TB_Mover.lastX,TB_Mover.targetY -TB_Mover.lastY);
    }

    private void updateBisector() { //Creates values for a line from the robot to the closet point on the projection line
        TB_Mover.bisectorSlope = (targetX -lastX)/(targetY -lastY);
        TB_Mover.bisectorIntersect = TB_Master.classes.odometer.Y-(-TB_Mover.bisectorSlope*TB_Master.classes.odometer.X);

        TB_Mover.bisectionX = -((TB_Mover.projectionIntersect-TB_Mover.bisectorIntersect)/(TB_Mover.projectionSlope+TB_Mover.bisectorSlope));
        TB_Mover.bisectionY = TB_Mover.projectionSlope*TB_Mover.bisectionX+TB_Mover.projectionIntersect;
    }

    private void updateTarget() { //Updates the heading the robot needs to follow to get to the target
        TB_Mover.targetHeading = Math.atan2(TB_Mover.targetX -TB_Master.classes.odometer.X,TB_Mover.targetY -TB_Master.classes.odometer.Y)-TB_Master.classes.odometer.Z;
    }

    private void updateCorrection() { //Updates the heading the robot needs to follow to get to the closet point on the path
        TB_Mover.correctionHeading = (Math.atan2(TB_Mover.bisectionX-TB_Master.classes.odometer.X,TB_Mover.bisectionY-TB_Master.classes.odometer.Y+0.0000001)-TB_Mover.projectionHeading)-TB_Master.classes.odometer.Z; //Added 0.001 to prevent undefined
        TB_Mover.correctionPower = distanceForm(TB_Mover.bisectionX,TB_Mover.bisectionY,TB_Master.classes.odometer.X,TB_Master.classes.odometer.Y);
        if (TB_Mover.lastY>TB_Mover.targetY) {
            TB_Mover.correctionHeading = -TB_Mover.correctionHeading;
        }
    }

    private void updateFinal() { //Combines all headings together and makes it robo readable
        TB_Mover.finalHeading = TB_Mover.targetHeading+(TB_Mover.correctionHeading*(Range.clip(TB_Mover.correctionPower/ TB_Config.correctionOverpowerDistance,0,TB_Config.correctionOverpowerDistance)));
        TB_Mover.axial = Math.cos(TB_Mover.finalHeading);
        TB_Mover.lateral = Math.sin(TB_Mover.finalHeading);
    }

    private void updateSpeed() {
        double deccel = Range.clip(TB_Master.classes.queue.getDistanceToEnd()*TB_Config.speedModifier,TB_Config.minSpeed,TB_Config.maxSpeed);

        double rotationModifier = Math.abs(TB_Mover.nextAngle-Math.PI)*(Math.PI*0.1);
        double caution = Range.clip(((12-TB_Master.classes.queue.getDistanceToCurrent())*TB_Config.speedModifier)*rotationModifier,0,TB_Config.maxSpeed-TB_Config.minSpeed);

        double accel = Range.clip(deccel-(TB_Master.classes.queue.getDistanceFromStart()*TB_Config.speedModifier)-TB_Config.startBoostSpeed,0,(TB_Config.maxSpeed-TB_Config.minSpeed));

        //Currently caution is removed "cel)-caution, TB_"
        TB_Mover.speed = Range.clip((deccel-accel),TB_Config.minSpeed,TB_Config.maxSpeed);
    }

    private void updateTargetYaw() {
        double waypointDistance = distanceForm(TB_Mover.lastX,TB_Mover.lastY,TB_Mover.targetX,TB_Mover.targetY);
        double distanceFromLast = distanceForm(TB_Mover.lastX,TB_Mover.lastY,TB_Mover.bisectionX,TB_Mover.bisectionY);
        double yawDistance = Math.abs(TB_Mover.lastZ-TB_Mover.targetZ);
        double progress = distanceFromLast/waypointDistance;
        TB_Mover.targetYaw = Range.clip(TB_Mover.lastZ+(yawDistance*progress),TB_Mover.lastZ,TB_Mover.targetZ);
    }

    private void updateYawPower() {
        TB_Mover.yawOff = Math.abs(TB_Mover.targetYaw-TB_Master.classes.odometer.Z);
        double tempYawPower = Range.clip((TB_Mover.targetYaw-TB_Master.classes.odometer.Z)/(Math.PI/5),-1,1);
        double multiplier = tempYawPower/Math.abs(tempYawPower);
        tempYawPower = Range.clip(Math.abs(tempYawPower),TB_Config.minSpeed,TB_Config.maxSpeed);
        TB_Mover.yawPower = tempYawPower*multiplier;
    }

    private double distanceForm(double x1, double y1, double x2, double y2) { //Simply returns the distance between two points
        return Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
    }

    protected String dataToString() {
        return ("Speed ="+TB_Mover.speed+"\n" +
                "LastX ="+TB_Mover.lastX+" LastY ="+TB_Mover.lastY+" LastZ ="+TB_Mover.lastZ+"\n" +
                "NextX ="+TB_Mover.targetX +" NextY"+TB_Mover.targetY +" NextZ ="+TB_Mover.targetZ +"\n" +
                "NextAngle ="+Math.toDegrees(TB_Mover.nextAngle)+"\n" +
                "ProjectionSlope ="+TB_Mover.projectionSlope+" ProjectionIntersect"+TB_Mover.projectionIntersect+" ProjectionHeading ="+TB_Mover.projectionHeading+"\n" +
                "BisectorSlope ="+TB_Mover.bisectorSlope+" BisectorIntersect ="+TB_Mover.bisectorIntersect+" BisectionX ="+TB_Mover.bisectionX+" BisectionY ="+TB_Mover.bisectionY+"\n" +
                "TargetHeading ="+Math.toDegrees(TB_Mover.targetHeading)+"\n" +
                "CorrectionHeading ="+Math.toDegrees(TB_Mover.correctionHeading)+" CorrectionPower ="+TB_Mover.correctionPower+"\n" +
                "FinalHeading ="+Math.toDegrees(TB_Mover.finalHeading)+" Axial ="+TB_Mover.axial+" Lateral="+TB_Mover.lateral+"\n" +
                "TargetYaw ="+Math.toDegrees(TB_Mover.targetYaw))+" YawPower ="+TB_Mover.yawPower+"\n" +
                "CycleCondition ="+cycleCondition()+"\n" +
                "DriveCondition ="+driveCondition()+"\n" +
                "RotateCondition ="+rotateCondition()+"\n" +
                "MoveCondition ="+moveCondition()+"\n" +
                "LockCondition ="+lockCondition();
    }

    protected void clearAll() {

        TB_Mover.lastX=0.0001; TB_Mover.lastY=0.0001; TB_Mover.lastZ=0;
        TB_Mover.targetX =0; TB_Mover.targetY =0; TB_Mover.targetZ =0;
        TB_Mover.projectionSlope=0; TB_Mover.projectionIntersect=0; TB_Mover.projectionHeading=0;
        TB_Mover.bisectorSlope=0; TB_Mover.bisectorIntersect=0; TB_Mover.bisectionX=0; TB_Mover.bisectionY=0;
        TB_Mover.targetHeading=0;
        TB_Mover.correctionHeading=0; TB_Mover.correctionPower=0;
        TB_Mover.finalHeading=0; TB_Mover.axial=0; TB_Mover.lateral=0;
        TB_Mover.speed=0;
        TB_Mover.targetYaw=0; TB_Mover.yawPower=0; TB_Mover.yawOff=0;
    }



    protected void smartMove() {
        double tempAxial = TB_Mover.axial;
        double tempLateral = TB_Mover.lateral;
        double tempYaw = TB_Mover.yawPower;

        if (driveCondition()) {
            tempAxial = 0;
            tempLateral = 0;
        }
        if (rotateCondition()) {
            tempYaw = 0;
        }

        movementPower(tempAxial,tempLateral,tempYaw,TB_Mover.speed);
    }

    //Set Motor Powers
    protected void movementPower(double axial, double lateral, double yaw, double speed) { //Sets the speed for individual motors based on input (Its lying about it always being zero.)
        //Creating Individual Power for Each Motor
        double frontLeftPower  = ((axial + lateral) * speed) + (yaw);
        double frontRightPower = ((axial - lateral) * speed) - (yaw);
        double backLeftPower   = ((axial - lateral) * speed) + (yaw);
        double backRightPower  = ((axial + lateral) * speed) - (yaw);

        //Set Motor Power
        TB_Master.drivetrain.frontLeft.setPower(frontLeftPower);
        TB_Master.drivetrain.frontRight.setPower(frontRightPower);
        TB_Master.drivetrain.backLeft.setPower(backLeftPower);
        TB_Master.drivetrain.backRight.setPower(backRightPower);
    }

    //Lock Motors
    private void lockDrivetrain() { //Locks all the wheels and applys pressure to account for looseness
        TB_Master.drivetrain.backLeft.setPower(TB_Config.stopForceSpeed);
        TB_Master.drivetrain.backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        TB_Master.drivetrain.frontRight.setPower(-TB_Config.stopForceSpeed);
        TB_Master.drivetrain.frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        TB_Master.drivetrain.backRight.setPower(TB_Config.stopForceSpeed);
        TB_Master.drivetrain.backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        TB_Master.drivetrain.frontLeft.setPower(-TB_Config.stopForceSpeed);
        TB_Master.drivetrain.frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    //Unlock Motors
    private void unlockDrivetrain() { //Releases the lock on the wheels
        movementPower(0,0,0,0);
        TB_Master.drivetrain.frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        TB_Master.drivetrain.frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        TB_Master.drivetrain.backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        TB_Master.drivetrain.backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }


}


