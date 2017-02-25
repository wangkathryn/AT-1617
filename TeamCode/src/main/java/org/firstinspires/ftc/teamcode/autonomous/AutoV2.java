package org.firstinspires.ftc.teamcode.autonomous;

/**
 * Created by davis on 2/21/17.
 */
public abstract class AutoV2 extends AutoBase{
  double WALL_DISTANCE = 7;
  public void run() throws InterruptedException {
    robot.colorSensor.enableLed(false);

    if (!settings.beacon1 && !settings.beacon2 && settings.numShots > 0) {
      int FORWARD_TICKS = 2700;
      int CAP_BALL_TICKS = 1000;
      driveTicks(-SPEED, FORWARD_TICKS);
      sleep(SLEEP_TIME*2);
      shootParticles();
      sleep(SLEEP_TIME);

      if (settings.knockCapBall) {
        driveTicks(-SPEED, CAP_BALL_TICKS);
      }

      stop();
      return;
    }

    driveTicks(-SPEED, 1000); // drive 1250 forward to shoot
    sleep(SLEEP_TIME);
    shootParticles();
    driveTicks(-SPEED, 250);
    sleep(SLEEP_TIME);
    rotateDegs(ROTATE_SPEED, getDir() == 1 ? 110 : 45); // rotate and move towards beacon
    sleep(SLEEP_TIME);

    driveTicks(SPEED * getDir(), 2400);
    sleep(SLEEP_TIME);

    rotateDegs(ROTATE_SPEED * getDir(), getDir() == 1 ? 47 : 40); // rotate into alignment with wall
    sleep(SLEEP_TIME);

    print("strafe");
    moveUntilCloserThan(WALL_DISTANCE, .8); // strafe until we're within pushing range
    sleep(SLEEP_TIME * 2);

    approachBeacon();
    sleep(SLEEP_TIME);
    pushButton((int)getDir());
    sleep(SLEEP_TIME);

    if (settings.beacon2) {
      driveTicks(FAST_SPEED*getDir(), 2200); // go with encoders fast until we're past the line, then approach beacon normally.
      approachBeacon();
      sleep(SLEEP_TIME);
      pushButton((int) getDir());
    }
  }
}
