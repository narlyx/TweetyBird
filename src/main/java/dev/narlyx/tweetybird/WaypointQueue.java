package dev.narlyx.tweetybird;

import java.util.ArrayList;

/**
 * Tracks and queues all waypoints internally for TweetyBird
 */
public class WaypointQueue {
  // Cache
  private final TweetyBird tweetyBird;
  private final ArrayList<Waypoint> queue = new ArrayList<>();
  private int currentIndex = 0;
  private boolean updated = false;

  /**
   * Constructor
   * @param tweetyBird Parent
   */
  public WaypointQueue(TweetyBird tweetyBird) {
    this.tweetyBird = tweetyBird;
    tweetyBird.log("Queue adding placeholder waypoint");
    addWaypoint(new Waypoint(tweetyBird.odometer.getX(),tweetyBird.odometer.getY(),tweetyBird.odometer.getZ()));
    tweetyBird.log("Queue setup");
  }

  /**
   * Adds a new waypoint to the end of the queue
   * @param waypoint New waypoint to be added
   */
  public void addWaypoint(Waypoint waypoint) {
    updated = true;
    queue.add(waypoint);
    tweetyBird.log("Queue new waypoint added X:"+waypoint.getX()+" Y:"+waypoint.getY()+" Z:"+waypoint.getZ());
  }

  /**
   * Adds a new waypoint to a specific index in queue
   * @param index Position in queue
   * @param waypoint New waypoint to be added
   */
  public void addWaypoint(int index, Waypoint waypoint) {
    updated = true;
    queue.add(index, waypoint);
    tweetyBird.log("Queue new waypoint added X:"+waypoint.getX()+" Y:"+waypoint.getY()+" Z:"+waypoint.getZ()+" at index:"+index);
  }

  /**
   * Moves on to the next waypoint in queue
   */
  public void increment() {
    tweetyBird.log("Queue increment called...");
    if (queue.size()>currentIndex+1) {
      currentIndex += 1;
      tweetyBird.log("Queue incremented");
    } else {
      tweetyBird.log("Queue not large enough to increment");
    }
  }

  /**
   * Clears out all waypoints accept for the current index
   */
  public void clear() {
    updated = true;
    //Waypoint currentWaypoint = getCurrentWaypoint();
    Waypoint currentWaypoint = new Waypoint(
            tweetyBird.odometer.getX(),
            tweetyBird.odometer.getY(),
            tweetyBird.odometer.getZ());
    queue.clear();
    queue.add(currentWaypoint);
    currentIndex = 0;
    tweetyBird.log("Queue cleared");
  }

  /**
   * Clears out waypoints in queue from 0 to current index (inclusive)
   */
  public void clearToCurrentIndex(){
    updated = true;
    ArrayList<Waypoint> tempQueue = new ArrayList<>();
    Waypoint currentWaypoint = new Waypoint(
            tweetyBird.odometer.getX(),
            tweetyBird.odometer.getY(),
            tweetyBird.odometer.getZ());
    for(int i = currentIndex; i < queue.size(); i++){
      tempQueue.add(getWaypoint(i));
    }
    queue.clear();
    queue.add(currentWaypoint);
    queue.addAll(tempQueue);
    currentIndex = 0;
    tweetyBird.log("Queue cleared up to current index");
  }

  /**
   * Returns the current index
   * @return Current index
   */
  public int getIndex() {
    updated = false;
    return currentIndex;
  }

  /**
   * Returns the size of the queue
   * @return Size of queue
   */
  public int getSize() {
    updated = false;
    return queue.size();
  }

  /**
   * Returns the waypoint under the current index
   * @return Current waypoint
   */
  public Waypoint getCurrentWaypoint() {
    updated = false;
    return queue.get(currentIndex);
  }

  /**
   * Returns a specific waypoint with a custom index
   * @param index Target waypoint index
   * @return Target waypoint
   */
  public Waypoint getWaypoint(int index) {
    updated = false;
    return queue.get(index);
  }

  /**
   * Returns weather or not the queue was updated since the last query
   * @return Updated since last query bool
   */
  public boolean getUpdated() {
    return updated;
  }


}
