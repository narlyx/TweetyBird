package dev.narlyx.tweetybird;

/**
 * Custom class used to store information about waypoints for TweetyBird internally
 */
public class Waypoint {

  /**
   * Internal variables
   */
  private double x = 0, y = 0, z = 0;

  /**
   * Constructor
   * @param x Initial x
   * @param y Initial y
   * @param z Initial z
   */
  public Waypoint(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;

  }

  /**
   * Returns stored X
   * @return Stored X
   */
  public double getX() {
    return x;
  }

  /**
   * Returns stored Y
   * @return Stored Y
   */
  public double getY() {
    return y;
  }

  /**
   * Returns stored Z
   * @return Stored Z
   */
  public double getZ() {
    return z;
  }
}