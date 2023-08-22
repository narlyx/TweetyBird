package com.chesterlk.ftc.TweetyBird.core;

import com.chesterlk.ftc.TweetyBird.util.TB_Waypoint;

import java.util.ArrayList;

public class TB_Queue {
    //List
    private ArrayList<TB_Waypoint> queue = new ArrayList<>(); //Position 0 will always be 'current'
    private ArrayList<TB_Waypoint> prevoius = new ArrayList<>();

    //Public Methods
    protected void add(TB_Waypoint newAddition) {
        TB_Mover.busy = true;
        queue.add(newAddition);
    }

    protected void silentAdd(TB_Waypoint newAddition) {
        queue.add(newAddition);
    }

    protected TB_Waypoint increment() {

        if (queue.size()==1) {
            prevoius.clear();
            return current();
        }


        //The next value if existent
        prevoius.add(0,current());
        queue.remove(0);
        return current();
    }

    protected TB_Waypoint current() {
        return queue.get(0);
    }

    protected TB_Waypoint next() {
        if (queue.size()==1) {
            return new TB_Waypoint(current().getX(), current().getY(), current().getZ());
        }

        return queue.get(1);
    }

    protected TB_Waypoint last() {
        if (prevoius.size()==0) {
            return new TB_Waypoint(TB_Master.classes.odometer.X,TB_Master.classes.odometer.Y,TB_Master.classes.odometer.Z);
        }
        return prevoius.get(0);
    }

    protected void clear() {
        queue.clear();
        prevoius.clear();
    }

    protected double getDistanceToCurrent() {
        return distanceForm(TB_Master.classes.odometer.X,TB_Master.classes.odometer.Y,current().getX(),current().getY());
    }

    protected double getDistanceToEnd() {
        double distance = getDistanceToCurrent();
        for (int i = 1; i<queue.size(); i++) {
            distance+=distanceForm(queue.get(i-1).getX(),queue.get(i-1).getY(),queue.get(i).getX(),queue.get(i).getY());
        }
        return distance;
    }

    protected double getDistanceFromStart() {
        double distance = distanceForm(last().getX(), last().getY(), TB_Master.classes.odometer.X,TB_Master.classes.odometer.Y);
        for (int i = 1; i<prevoius.size(); i++) {
            distance+=distanceForm(prevoius.get(i-1).getX(),prevoius.get(i-1).getY(),prevoius.get(i).getX(),prevoius.get(i).getY());
        }
        return distance;
    }

    private static double distanceForm(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
    }
}
