package dev.narlyx.tweetybird;

import java.util.ArrayList;

/**
 * Tracks and queues all waypoints internally for TweetyBird
 */
public class WaypointQueue {
  // Cache
  private TweetyBird tweetyBird;
  private ArrayList<Waypoint> queue = new ArrayList<>();
  private int currentIndex = 0;
  private boolean updated = false;

  /**
   * Constructor
   * @param tweetyBird Parent
   */
  public WaypointQueue(TweetyBird tweetyBird) {
    this.tweetyBird = tweetyBird;
    tweetyBird.sendDebugMessage("Queue adding placeholder waypoint");
    addWaypoint(new Waypoint(0,0,0));
    tweetyBird.sendDebugMessage("Queue setup");
  }

  /**
   * Adds a new waypoint to the end of the queue
   * @param waypoint New waypoint to be added
   */
  public void addWaypoint(Waypoint waypoint) {
    updated = true;
    queue.add(waypoint);
    tweetyBird.sendDebugMessage("Queue new waypoint added X:"+waypoint.getX()+" Y:"+waypoint.getY()+" Z:"+waypoint.getZ());
  }

  /**
   * Moves on to the next waypoint in queue
   */
  public void increment() {
    tweetyBird.sendDebugMessage("Queue increment called...");
    if (queue.size()>currentIndex+1) {
      currentIndex += 1;
      tweetyBird.sendDebugMessage("Queue incremented");
    } else {
      tweetyBird.sendDebugMessage("Queue not large enough to increment");
    }
  }

  /**
   * Clears out all waypoints accept for the current index
   */
  public void clear() {
    Waypoint currentWaypoint = getCurrentWaypoint();
    queue.clear();
    queue.add(currentWaypoint);
    currentIndex = 0;
    tweetyBird.sendDebugMessage("Queue cleared");
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
