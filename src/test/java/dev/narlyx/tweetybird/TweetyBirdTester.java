package dev.narlyx.tweetybird;

import static java.lang.Thread.sleep;

import org.junit.Test;

public class TweetyBirdTester {

  @Test
  public void mainTest() throws InterruptedException {
    // Global cache
    final double[] globalX = {0};
    final double[] globalY = {0};
    final double[] globalZ = {0};

    // Creating odometer
    class TestOdometer implements TweetyBird.Odometer{
      @Override
      public double getX() {
        return globalX[0];
      }

      @Override
      public double getY() {
        return globalY[0];
      }

      @Override
      public double getZ() {
        return globalZ[0];
      }

      @Override
      public void resetTo(double x, double y, double z) {
        globalX[0] = x;
        globalY[0] = y;
        globalZ[0] = z;
      }
    }
    TestOdometer testOdometer = new TestOdometer();

    // Creating driver
    class TestDriver implements TweetyBird.Driver {
      @Override
      public void setHeading(double y, double x, double z, double speed) {
        System.out.println("Moving");
        globalX[0] += x * speed;
        globalY[0] += y * speed;
        globalZ[0] += z;
      }

      @Override
      public void stopAndHold() {
        System.out.println("Holding");
      }
    }
    TestDriver testDriver = new TestDriver();

    // Pulling TweetyBird
    TweetyBird tweetyBird = new TweetyBird.Builder()
        .setOdometer(testOdometer)
        .setDriver(testDriver)
        .setMinimumSpeed(0.2)
        .setMaximumSpeed(0.8)
        .setDebuggingEnabled(true)
        .build();

    tweetyBird.sendTargetPosition(32,0,0);
    tweetyBird.sendTargetPosition(32,10,0);

    while (tweetyBird.isBusy()) {
    }
    tweetyBird.close();
  }
}

