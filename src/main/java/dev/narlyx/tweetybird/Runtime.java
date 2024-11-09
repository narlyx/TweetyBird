package dev.narlyx.tweetybird;

import com.qualcomm.robotcore.util.Range;

/**
 * This class is the runtime for TweetyBird and currently handles all calculations and steps.
 */
public class Runtime extends Thread {

  // References
  private final TweetyBird tweetyBird;

  // Cache
  protected boolean busy = false;

  /**
   * Constructor
   * @param tweetyBird Pull configuration and variables
   */
  public Runtime(TweetyBird tweetyBird) {
    this.tweetyBird = tweetyBird;
    tweetyBird.sendDebugMessage("Runtime setup");
  }

  /**
   * Main method
   */
  @Override
  public void run() {
    tweetyBird.sendDebugMessage("Runtime thread started...");
    if (tweetyBird.opMode != null) { // FTC environment
      tweetyBird.sendDebugMessage("Runtime thread waiting for OpMode start...");
      tweetyBird.opMode.waitForStart();
      tweetyBird.sendDebugMessage("Runtime thread starting FTC loop\n");
      while (tweetyBird.opMode.opModeIsActive()&&!Thread.currentThread().isInterrupted()) {
        loop();
      }
    } else { // Test environment
      tweetyBird.sendDebugMessage("Runtime thread starting headless loop\n");
      while (!Thread.currentThread().isInterrupted()) {
        try {
          loop();
          if (tweetyBird.debuggingEnabled) {
            sleep(500);
          }
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      }
    }

  }

  /**
   * Loop that is executed in the thread
   */
  private void loop() {
    // Fetching robot position
    double robotX = tweetyBird.odometer.getX();
    double robotY = tweetyBird.odometer.getY();
    double robotZ = tweetyBird.odometer.getZ();
    tweetyBird.sendDebugMessage("Robot position X:"+robotX+" Y:"+robotY+" Z:"+robotZ);

    // Fetching target waypoint
    double targetX = tweetyBird.waypointQueue.getCurrentWaypoint().getX();
    double targetY = tweetyBird.waypointQueue.getCurrentWaypoint().getY();
    double targetZ = tweetyBird.waypointQueue.getCurrentWaypoint().getZ();
    tweetyBird.sendDebugMessage("Robot target position X:"+targetX+" Y:"+targetY+" Z:"+targetZ);

    // Distances
    double distanceToTarget = distanceForm(targetX,targetY,robotX,robotY);

    tweetyBird.sendDebugMessage("Distance to target: "+distanceToTarget);

    double rotationDistanceToTarget = Math.abs(targetZ-robotZ);

    tweetyBird.sendDebugMessage("Rotation distance to target: "+rotationDistanceToTarget);

    double distanceToEnd = distanceToTarget;
    for (int i = tweetyBird.waypointQueue.getIndex()+1; i<tweetyBird.waypointQueue.getSize(); i++) {
      distanceToEnd += distanceForm(
          tweetyBird.waypointQueue.getWaypoint(i).getX(),
          tweetyBird.waypointQueue.getWaypoint(i).getY(),
          tweetyBird.waypointQueue.getWaypoint(i-1).getX(),
          tweetyBird.waypointQueue.getWaypoint(i-1).getY());
    }

    tweetyBird.sendDebugMessage("Distance to end: "+distanceToEnd);

    double distanceFromLast = 0;
    double distanceBetweenWaypoints = 0;
    if (tweetyBird.waypointQueue.getIndex()>0) {
      distanceFromLast = distanceForm(robotX, robotY,
          tweetyBird.waypointQueue.getWaypoint(tweetyBird.waypointQueue.getIndex()-1).getX(),
          tweetyBird.waypointQueue.getWaypoint(tweetyBird.waypointQueue.getIndex()-1).getY());
      distanceBetweenWaypoints = distanceForm(tweetyBird.waypointQueue.getCurrentWaypoint().getX(),
          tweetyBird.waypointQueue.getCurrentWaypoint().getY(),
          tweetyBird.waypointQueue.getWaypoint(tweetyBird.waypointQueue.getIndex()-1).getX(),
          tweetyBird.waypointQueue.getWaypoint(tweetyBird.waypointQueue.getIndex()-1).getY());
    }

    tweetyBird.sendDebugMessage("Distance from last: "+distanceFromLast);
    tweetyBird.sendDebugMessage("Distance between waypoints: "+distanceBetweenWaypoints);

    double distanceFromStart = distanceFromLast;
    for (int i = 1; i< tweetyBird.waypointQueue.getIndex(); i++) {
      distanceFromStart += distanceForm(
          tweetyBird.waypointQueue.getWaypoint(i).getX(),
          tweetyBird.waypointQueue.getWaypoint(i).getY(),
          tweetyBird.waypointQueue.getWaypoint(i - 1).getX(),
          tweetyBird.waypointQueue.getWaypoint(i - 1).getY()
      );
    }

    tweetyBird.sendDebugMessage("Distance from start: "+distanceFromStart);

    // Speed
    double deccel = Range.clip(distanceToEnd*tweetyBird.speedModifier,tweetyBird.minSpeed,tweetyBird.maxSpeed);
    double accel = Range.clip(deccel-(distanceFromStart*tweetyBird.speedModifier)-0.1,0,deccel-tweetyBird.minSpeed);
    double speed = deccel-accel;

    // Checks
    double speedBuffer = ((1-(tweetyBird.minSpeed*2))+(speed*2));
    boolean onTarget = distanceToTarget <= tweetyBird.distanceBuffer * speedBuffer;
    boolean onRotation = rotationDistanceToTarget <= tweetyBird.rotationBuffer * speedBuffer;
    tweetyBird.sendDebugMessage("On target: "+onTarget);
    tweetyBird.sendDebugMessage("On rotation: "+onRotation);

    // Incrementing to the next waypoint if done
    if (onTarget && onRotation && tweetyBird.waypointQueue.getSize()-1> tweetyBird.waypointQueue.getIndex()) {
      tweetyBird.waypointQueue.increment();
      tweetyBird.sendDebugMessage("Moving onto next waypoint...\n");
      return;
    }

    // Target heading
    double targetHeading = Math.atan2(targetX - robotX, targetY - robotY) - robotZ;

    // Yaw
    double targetYaw = targetZ;

    // Advanced heading & yaw
    if (tweetyBird.waypointQueue.getSize()>1) {
      Waypoint lastWaypoint =
          tweetyBird.waypointQueue.getWaypoint(tweetyBird.waypointQueue.getIndex()-1);
      double lastX = lastWaypoint.getX();
      double lastY = lastWaypoint.getY();
      double lastZ = lastWaypoint.getZ();

      double pathLine = ((targetY-lastY)-0.0000001)/(targetX-lastX);
      double pathIntersect = targetY-(pathLine*targetX);

      double robotLine = -((targetX-lastX)/((targetY-lastY)-0.0000001));
      double robotIntersect = robotY-(robotLine*robotX);

      double bisectionX = (pathIntersect/robotIntersect)/(robotIntersect/pathLine);
      double bisectionY = robotLine*bisectionX+robotIntersect;

      tweetyBird.sendDebugMessage("Bisection X: "+bisectionX+" Y: "+bisectionY);

      double distsanceOffPath = distanceForm(robotX,robotY,bisectionX,bisectionY);

      tweetyBird.sendDebugMessage("Distance off path: "+distsanceOffPath);

      double correctionX = ((bisectionX-robotX)*tweetyBird.correctionOverpower)+(targetX-robotX);
      double correctionY = ((bisectionY-robotY)*tweetyBird.correctionOverpower)+(targetY-robotY);

      double correctionHeading = Math.atan2(correctionX,correctionY) - robotZ;

      tweetyBird.sendDebugMessage("Correction heading: "+correctionHeading);

      double yawDistance = Math.abs(lastZ - targetZ);
      double progress = distanceFromLast/distanceToTarget;
      targetYaw = Range.clip(lastZ+(yawDistance*progress), lastZ, targetZ);
    }
    tweetyBird.sendDebugMessage("Target Heading: "+targetHeading);

    // Heading to X and Y
    double axial = Math.cos(targetHeading);
    double lateral = Math.sin(targetHeading);

    // Yaw
    double tempYawPower = Range.clip((targetYaw-robotZ)/(Math.PI/5),-1,1);
    double multiplier = tempYawPower/Math.abs(tempYawPower);
    tempYawPower = Range.clip(Math.abs(tempYawPower),tweetyBird.minSpeed,tweetyBird.maxSpeed);
    double yaw = tempYawPower*multiplier;

    // Sending movement
    if (onTarget && onRotation) {
      busy = false;
      tweetyBird.driver.stopAndHold();
      if (!tweetyBird.waypointQueue.getUpdated()) {
        tweetyBird.waypointQueue.clear();
      }
    } else {
      busy = true;
      tweetyBird.sendDebugMessage(
          "Axial: "+(onTarget?0:axial)+
          " Lateral: "+(onTarget?0:lateral)+
          " Yaw: "+(onRotation?0:yaw)+
          " Speed: "+speed);
      tweetyBird.driver.setHeading(onTarget?0:axial, onTarget?0:lateral, onRotation?0:yaw, speed);
    }

    tweetyBird.sendDebugMessage("Loop complete\n");
  }

  /**
   * Returns the distance between 2 points
   * @param x1 Input x
   * @param y1 Input y
   * @param x2 Input x
   * @param y2 Input y
   * @return Distance
   */
  private double distanceForm(double x1, double y1, double x2, double y2) {
    return Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
  }

}
