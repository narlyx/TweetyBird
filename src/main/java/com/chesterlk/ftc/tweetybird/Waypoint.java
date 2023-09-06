package com.chesterlk.ftc.tweetybird;

public class Waypoint {

    //Variables
    private double x = 0;
    private double y = 0;
    private double z = 0;


    //Constructor
    public Waypoint(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

    }

    //Return Variables
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
