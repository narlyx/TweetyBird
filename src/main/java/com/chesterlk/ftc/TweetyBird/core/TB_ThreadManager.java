package com.chesterlk.ftc.TweetyBird.core;

import com.chesterlk.ftc.TweetyBird.util.TB_Waypoint;

public class TB_ThreadManager extends Thread {
    //States
    protected boolean loaded = false;
    protected boolean readyToStop = false;

    //Classes
    TB_Odometer odometer = null;
    TB_Mover mover = null;
    TB_Queue queue = null;

    @Override
    public void run() {
        //Creating Classes
        queue = new TB_Queue();
        queue.clear();
        odometer = new TB_Odometer();
        odometer.start();
        mover = new TB_Mover();
        mover.start();

        queue.silentAdd(new TB_Waypoint(TB_Master.classes.odometer.X,TB_Master.classes.odometer.Y,TB_Master.classes.odometer.Z));

        try {
            sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        loaded = true;

        TB_Master.opMode.waitForStart();

        while (!readyToStop) {}

        try {
            sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        odometer = null;
        mover = null;
        queue = null;
        TB_Master.classes = null;
    }

}
