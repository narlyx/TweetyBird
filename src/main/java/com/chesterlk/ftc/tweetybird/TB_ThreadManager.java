package com.chesterlk.ftc.tweetybird;

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
        //Creating Classes and making sure it has fresh data
        queue = new TB_Queue();
        queue.clear();
        odometer = new TB_Odometer();
        odometer.start();
        mover = new TB_Mover();
        mover.start();

        //Adding base waypoint (0,0) since the queue was cleared above
        queue.silentAdd(new TB_Waypoint(TB_Master.classes.odometer.X,TB_Master.classes.odometer.Y,TB_Master.classes.odometer.Z));

        try { //Wait for 10 milliseconds for the above to work out
            sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Signalling to TB_Mover everything is ready
        loaded = true;

        //Waiting for opmode to start
        TB_Master.opMode.waitForStart();

        //Waiting for opmode to stop
        while (!readyToStop) {}

        //10 milliseconds to allow TB_Mover to catch up
        try {
            sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //"Killing" all of the classes
        odometer = null;
        mover = null;
        queue = null;
        TB_Master.classes = null;

        //TODO: Replace waits with a more uniform check
    }

}
