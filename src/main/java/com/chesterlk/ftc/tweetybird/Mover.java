package com.chesterlk.ftc.tweetybird;

import static java.lang.Double.isNaN;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

/**
 * Mover class, responsible for controlling the drivetrain and preforming calculations
 */

public class Mover extends Thread {

    //Processor
    TweetyBirdProcessor processor;

    //States
    /**
     * Status variable determining weather the robot is busy or not
     */
    protected boolean busy = false;
    /**
     * Status variable determining weather the drivetrain will be controlled or not
     */
    protected boolean engaged = false;
    /**
     * Status variable used to start and stop the mover thread
     */
    protected boolean running;
    /**
     * Status variable to request the mover class to stop
     */
    private boolean stopRequested;

    //Data
    private double
    lastX=0.0001, lastY=0.0001, lastZ=0,
    targetX =0, targetY =0, targetZ =0,
    nextX =0, nextY =0, nextZ =0,
    projectionSlope=0, projectionIntersect=0, projectionHeading=0,
    bisectorSlope=0, bisectorIntersect=0, bisectionX=0, bisectionY=0,
    targetHeading=0,
    correctionHeading=0, correctionPower=0,
    finalHeading=0, axial=0, lateral=0,
    speed=0,
    targetYaw=0,yawPower=0,yawOff=0,
    nextAngle=0
        ;

    /**
     * Constructor class to prepare for startus
     * @param processor
     */
    public Mover(TweetyBirdProcessor processor) {
        this.processor = processor;
        stopRequested = false;
        running = false;
    }


    //Thread
    @Override
    public void run() {
        //Status
        stopRequested = false;
        running = true;

        //Wait for start
        processor.opMode.waitForStart();

        //Runtime Loop
        while (processor.opMode.opModeIsActive()||!stopRequested) {


            //Updating Values such as the position
            if (updateCondition()) {
                    updateCoreValues();
            }

            //Getting next waypoint if needed and setting waypoint values
            if (cycleCondition()) {
                cycleWaypoint();
            }

            //Action
            if (moveCondition()) { //Move Robot
                smartMove();
                busy = true;

            } else if (lockCondition()) { //Lock Robot
                lockDrivetrain();
                busy = false;

            } else { //"Relax" Robot
                //unlockDrivetrain();
                busy = false;
            }

            //Getting next waypoint if needed and setting waypoint values (Checks twice)
            if (cycleCondition()) {
                cycleWaypoint();
            }



        }
        if(!stopRequested){
            processor.stop();
        }
        running = false;
    }

    /**
     * When called, the thread will terminate
     */
    protected void requestStop() {
        stopRequested = true;
    }





    //Class Conditions
    private boolean cycleCondition() { //True if TweetyBird can continue
        return processor.queue.getDistanceToCurrent()<3&driveCondition()&rotateCondition();
    }

    private boolean moveCondition() { //True if the robot is allowed to move
        return engaged&!lockCondition();
    }

    private boolean driveCondition() { //True if the robot's x and y pos is correct
        return distanceForm(targetX, targetY, processor.odometer.X,processor.odometer.Y)<=0.6;
    }

    private boolean rotateCondition() { //True is the robot's z pos is correct
        return yawOff<0.08;
    }

    private boolean lockCondition() { //True if the robot is in the correct spot
        return engaged&driveCondition()&rotateCondition();
    }

    private boolean updateCondition() { //True if TweetyBird is ready
        return busy&&engaged;
    }






    //Bulk Update Data

    /**
     * Cycles to the next waypoint in queue and prepares the mover class
     */
    protected void cycleWaypoint() { //Cycles values to allow the robot to continue
        processor.queue.increment();
        setLast(processor.queue.last().getX(),processor.queue.last().getY(),processor.queue.last().getZ());
        setTarget(processor.queue.current().getX(),processor.queue.current().getY(),processor.queue.current().getZ());
        setNext(processor.queue.next().getX(),processor.queue.next().getY(),processor.queue.next().getZ());
        updateProjection();
    }

    /**
     * Bulk updates values that must be refreshed every cycle
     */
    private void updateCoreValues() { //Updates the values needed during every loop
        updateBisector();
        updateTarget();
        updateCorrection();
        updateFinal();
        updateSpeed();
        updateTargetYaw();
        updateYawPower();
        updateNextAngle();
    }




    //Update Individual Sections Data
    private void setLast(double x, double y, double z) { //Last Waypoint
        lastX = x;
        lastY = y;
        lastZ = z;
    }

    private void setTarget(double x, double y, double z) { //Target Waypoint
        targetX = x== lastX?x+0.0001:x;
        targetY = y== lastY?y+0.0001:y;
        targetZ = z;
    }

    private void setNext(double x, double y, double z) {
        nextX = x== lastX?x+0.0001:x;
        nextY = y== lastY?y+0.0001:y;
        nextZ = z;
    }

    private void updateNextAngle() {
        double a = Math.abs(distanceForm(targetX,targetY,nextX,nextY));
        double b = Math.abs(distanceForm(lastX,lastY,targetX,targetY));
        double c = Math.abs(distanceForm(lastX,lastY,nextX,nextY)-0.0001);

        nextAngle = Math.acos( (a*a+b*b-c*c) / (2.0*a*b) );

        if(isNaN(nextAngle)) {
            nextAngle = Math.toRadians(1);
        }
    }

    private void updateProjection() { //Creates values for a line from last to target
        projectionSlope = (targetY -lastY)/(targetX -lastX);
        projectionIntersect = lastY-(projectionSlope*lastX);
        correctionHeading = Math.atan2(targetX - lastX, targetY - lastY);
    }

    private void updateBisector() { //Creates values for a line from the robot to the closet point on the projection line
        bisectorSlope = (targetX -lastX)/(targetY -lastY);
        bisectorIntersect = processor.odometer.Y-(-bisectorSlope*processor.odometer.X);

        bisectionX = -((projectionIntersect- bisectorIntersect)/(projectionSlope+ bisectorSlope));
        bisectionY = projectionSlope* bisectionX+ projectionIntersect;
    }

    private void updateTarget() { //Updates the heading the robot needs to follow to get to the target
        targetHeading = Math.atan2(targetX -processor.odometer.X, targetY -processor.odometer.Y)-processor.odometer.Z;
    }

    private void updateCorrection() { //Updates the heading the robot needs to follow to get to the closet point on the path
        correctionHeading = (Math.atan2(bisectionX-processor.odometer.X, bisectionY-processor.odometer.Y+0.0000001)- projectionHeading)-processor.odometer.Z; //Added 0.001 to prevent undefined
        correctionPower = distanceForm(bisectionX, bisectionY,processor.odometer.X,processor.odometer.Y);
        if (lastY> targetY) {
            correctionHeading = -correctionHeading;
        }
    }

    private void updateFinal() { //Combines all headings together and makes it robo readable
        finalHeading = targetHeading+(correctionHeading*(Range.clip(correctionPower/ processor.correctionOverpowerDistance,0,processor.correctionOverpowerDistance)));
        axial = Math.cos(finalHeading);
        lateral = Math.sin(finalHeading);
    }

    private void updateSpeed() {
        double deccel = Range.clip(processor.queue.getDistanceToEnd()*processor.speedModifier,processor.minSpeed,processor.maxSpeed);

        double rotationModifier = Math.abs(nextAngle-Math.PI)*(Math.PI*0.1);
        double caution = Range.clip(((12-processor.queue.getDistanceToCurrent())*processor.speedModifier)*rotationModifier,0,processor.maxSpeed-processor.startSpeed);

        double accel = Range.clip(deccel-(processor.queue.getDistanceFromStart()*processor.speedModifier)-processor.startSpeed,0,(processor.maxSpeed-processor.minSpeed));

        //Currently caution is removed "cel)-caution, TB_"
        speed = Range.clip((deccel-accel)-caution,processor.minSpeed,processor.maxSpeed);
    }

    private void updateTargetYaw() {
        double waypointDistance = distanceForm(lastX, lastY, targetX, targetY);
        double distanceFromLast = distanceForm(lastX, lastY, bisectionX, bisectionY);
        double yawDistance = Math.abs(lastZ- targetZ);
        double progress = distanceFromLast/waypointDistance;
        targetYaw = Range.clip(lastZ+(yawDistance*progress), lastZ, targetZ);
    }

    private void updateYawPower() {
        yawOff = Math.abs(targetYaw-processor.odometer.Z);
        double tempYawPower = Range.clip((targetYaw-processor.odometer.Z)/(Math.PI/5),-1,1);
        double multiplier = tempYawPower/Math.abs(tempYawPower);
        tempYawPower = Range.clip(Math.abs(tempYawPower),processor.minSpeed,processor.maxSpeed);
        yawPower = tempYawPower*multiplier;
    }

    private double distanceForm(double x1, double y1, double x2, double y2) { //Simply returns the distance between two points
        return Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
    }

    /**
     * Will apply power from the above calulations
     */
    protected void smartMove() { //Will only move certain areas based on data
        double tempAxial = axial;
        double tempLateral = lateral;
        double tempYaw = yawPower;

        if (driveCondition()) {
            tempAxial = 0;
            tempLateral = 0;
        }
        if (rotateCondition()) {
            tempYaw = 0;
        }

        movementPower(tempAxial,tempLateral,tempYaw, speed);
    }

    /**
     * Will convert a vector input to power to the drivetrain
     * @param axial Forward and backward inout
     * @param lateral Side to side input
     * @param yaw Rotational input (clockwise is positive)
     * @param speed Speed input
     */
    protected void movementPower(double axial, double lateral, double yaw, double speed) { //Sets the speed for individual motors based on input (Its lying about it always being zero.)
        //Creating Individual Power for Each Motor
        double frontLeftPower  = ((axial + lateral) * speed) + (yaw);
        double frontRightPower = ((axial - lateral) * speed) - (yaw);
        double backLeftPower   = ((axial - lateral) * speed) + (yaw);
        double backRightPower  = ((axial + lateral) * speed) - (yaw);

        //Set Motor Power
        processor.fl.setPower(frontLeftPower);
        processor.fr.setPower(frontRightPower);
        processor.bl.setPower(backLeftPower);
        processor.br.setPower(backRightPower);
    }

    //Lock Motors
    private void lockDrivetrain() { //Locks all the wheels and applys pressure to account for looseness
        processor.bl.setPower(processor.stopForceSpeed);
        processor.bl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        processor.fr.setPower(-processor.stopForceSpeed);
        processor.fr.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        processor.br.setPower(processor.stopForceSpeed);
        processor.br.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        processor.fl.setPower(-processor.stopForceSpeed);
        processor.fl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    //Unlock Motors
    private void unlockDrivetrain() { //Releases the lock on the wheels
        movementPower(0,0,0,0);
        processor.fl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        processor.fr.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        processor.bl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        processor.br.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }


}


