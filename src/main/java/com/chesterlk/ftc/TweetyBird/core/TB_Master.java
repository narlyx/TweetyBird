package com.chesterlk.ftc.tweetybird.core;

import com.chesterlk.ftc.tweetybird.util.TB_DrivetrainHwmap;
import com.chesterlk.ftc.tweetybird.util.TB_Waypoint;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

public class TB_Master extends TB_Mover {

    //OpMode
    public static LinearOpMode opMode; //Will be set during constructor

    //Thread Manager
    static TB_ThreadManager classes = null;

    //Hardware Map
    public static TB_DrivetrainHwmap drivetrain = new TB_DrivetrainHwmap();


    //Other
    private ElapsedTime runtime = new ElapsedTime();

    //Initialization Step
    public TB_Master(LinearOpMode opMode) {
        //Setting OpMode Variable
        TB_Master.opMode = opMode;

        //Init Drivetrain
        drivetrain.init(opMode);

        //Classes
        classes = new TB_ThreadManager();
        classes.start();

        //Waiting for all threads to start, terminate after 5 seconds
        runtime.reset();
        while (!classes.loaded&opMode.opModeIsActive()) {
            if (runtime.seconds()>=5) {
                break;
            }
        }

        TB_Mover.engaged = true;

    }

    //Interface
    public void addWaypoint(double y, double x, double z) {
        TB_Mover.busy = true;
        classes.queue.add(new TB_Waypoint(x,y,Math.toRadians(z)));

    }

    public boolean isBusy() {
        return TB_Mover.busy;
    }

    public void waitWhileBusy() {
        while (opMode.opModeIsActive()&&isBusy()) {
            opMode.sleep(0);
        }
    }

    public void toggleEngagement() {
        if (TB_Mover.engaged) {
            TB_Mover.engaged = false;
        } else {
            TB_Mover.engaged = true;
        }
    }

    public double getX() {
        return classes.odometer.X;
    }

    public double getY() {
        return classes.odometer.Y;
    }

    public double getZ() {
        return classes.odometer.Z;
    }

    public void clear() {
        classes.queue.clear();
    }



}
