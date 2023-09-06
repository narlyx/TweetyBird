package com.chesterlk.ftc.tweetybird;

import java.util.ArrayList;

public class Queue {

    //Status
    protected boolean running;
    protected boolean waypointReceived=false;

    //Processor
    private TweetyBirdProcessor processor;

    //List
    private ArrayList<Waypoint> queue; //Position 0 will always be 'current'
    private ArrayList<Waypoint> prevoius;

    //Constructor
    public Queue(TweetyBirdProcessor processor) {
        running = false;
        this.processor = processor;

        //Clearing Variables
        queue = new ArrayList<>();
        prevoius = new ArrayList<>();
    }

    //Public Methods
    protected void add(Waypoint newAddition) { //Adds new waypoint to the list
        processor.mover.busy = true;
        queue.add(newAddition);
        waypointReceived = true;
    }

    protected void silentAdd(Waypoint newAddition) { //Adds a new waypoint to the list, but does nto start TweetyBird
        queue.add(newAddition);
        waypointReceived = true;
    }

    protected Waypoint increment() { //Moves current to past, then grabs the next waypoint in queue to be current

        if (queue.size()==1) {
            prevoius.clear();
            return current();
        }


        //The next value if existent
        prevoius.add(0,current());
        queue.remove(0);
        return current();
    }

    protected Waypoint current() { //Returns current waypoint
        running = true;
        return queue.get(0);
    }

    protected Waypoint next() { //Returns next waypoint
        if (queue.size()==1) {
            return new Waypoint(current().getX(), current().getY(), current().getZ());
        }

        return queue.get(1);
    }

    protected Waypoint last() { //Returns last waypoint
        if (prevoius.size()==0) {
            return new Waypoint(processor.odometer.X-0.000001,processor.odometer.Y-0.000001,processor.odometer.Z-0.000001);
        }
        return prevoius.get(0);
    }

    protected void clear() { //Clears waypoint list TODO: Currently does not integrate well with TB_Mover
        queue.clear();
        prevoius.clear();
        silentAdd(new Waypoint(processor.odometer.X,processor.odometer.Y,processor.odometer.Z));
        running = true;
    }

    protected double getDistanceToCurrent() { //Returns distance between robot to the current waypoint
        return distanceForm(processor.odometer.X,processor.odometer.Y,current().getX(),current().getY());
    }

    protected double getDistanceToEnd() { //Returns currentDistance + the distance between each waypoint until the end
        double distance = getDistanceToCurrent();
        for (int i = 1; i<queue.size(); i++) {
            distance+=distanceForm(queue.get(i-1).getX(),queue.get(i-1).getY(),queue.get(i).getX(),queue.get(i).getY());
        }
        return distance;
    }

    protected double getDistanceFromStart() { //Returns all of the previous waypoints
        double distance = distanceForm(last().getX(), last().getY(), processor.odometer.X,processor.odometer.Y);
        for (int i = 1; i<prevoius.size(); i++) {
            distance+=distanceForm(prevoius.get(i-1).getX(),prevoius.get(i-1).getY(),prevoius.get(i).getX(),prevoius.get(i).getY());
        }
        return distance;
    }

    private static double distanceForm(double x1, double y1, double x2, double y2) { //Function to clean up code (distance formula)
        return Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
    }
}
