package org.firstinspires.ftc.teamcode.autonomous;

import android.test.InstrumentationTestRunner;

import com.google.gson.Gson;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ReadWriteFile;

import org.firstinspires.ftc.robotcore.internal.AppUtil;
import org.firstinspires.ftc.teamcode.chassis.Orion;
import org.firstinspires.ftc.teamcode.sensors.Vuforia;

import java.io.File;
import java.io.IOException;

/**
 * Created by davis on 11/25/16.
 */
public abstract class AutoBase extends LinearOpMode {
  Orion robot;
  Settings settings;

  int SLEEP_TIME = 300;
  int NUM_PUSHES = 1;
  double SPEED = 0.4;
  double FAST_SPEED = 0.8;
  double STRAFE_SPEED = 0.6;
  double ROTATE_SPEED = 0.4;


  void setup() throws InterruptedException{
    Gson gson = new Gson();
    File sfile = AppUtil.getInstance().getSettingsFile("auto_settings.json");
    try {
      settings = gson.fromJson(ReadWriteFile.readFileOrThrow(sfile), Settings.class);
    } catch (IOException e) {
      settings = new Settings();
    }
    robot = new Orion();
    robot.init(hardwareMap);
    while (!robot.catapultLoaded())
      robot.runChoo(1);
    robot.runChoo(0);
    telemetry.addData("Delay (seconds)", settings.delay);
    telemetry.addData("Hit beacon 1", settings.beacon1);
    telemetry.addData("Hit beacon 2", settings.beacon2);
    telemetry.addData("Shoot how many particles", settings.numShots);
    telemetry.addData("Knock off cap ball", settings.knockCapBall);
    telemetry.addData("End on center", settings.endOnCenter);
    telemetry.update();
    telemetry.update();
  }

  abstract void run() throws InterruptedException;

  public void runOpMode() throws InterruptedException{
    setup();
    waitForStart();
    telemetry.addData(">", settings.delay + " second delay");
    telemetry.update();
    sleep(settings.delay*1000);
    telemetry.update();
    run();
  }

  abstract double getDir();


  void transferParticle() throws InterruptedException{
    double chamberPos = robot.PIVOT_LOADBALL;
    while (opModeIsActive() && chamberPos > robot.PIVOT_HITRIGHT) {
      chamberPos -= .01;
      robot.pivot(chamberPos);
      idle();
    }
    sleep(150);
    while (opModeIsActive() && chamberPos < robot.PIVOT_LOADBALL) {
      chamberPos += .01;
      robot.pivot(chamberPos);
      idle();
    }
  }
  void fireParticle() throws InterruptedException {
    while (!robot.catapultLoaded() && opModeIsActive())
      robot.runChoo(1);
    while (robot.catapultLoaded() && opModeIsActive())
      robot.runChoo(1);
    while (!robot.catapultLoaded() && opModeIsActive())
      robot.runChoo(1);
    robot.runChoo(0);
  }

  /**
   *
   * @param pow power
   * @param ticks number of ticks forward
   * @param timeout seconds before you stop moving if encoders don't finish
   */
  void driveTicks(double pow, int ticks, int timeout) {
    double angle = pow > 0 ? 0 : Math.PI;
    robot.resetTicks();
    robot.imu.update();
    double h = robot.imu.heading();
    long startTime = System.currentTimeMillis();
    long currentTime = startTime;
    while (Math.abs(robot.getTicks()) < ticks && currentTime - startTime < timeout && opModeIsActive()) {
      robot.imu.update();
      robot.moveStraight(Math.abs(pow), angle, robot.imu.heading(), h);
      currentTime = System.currentTimeMillis();
    }
    robot.stopMotors();
  }

  void driveTicks(double pow, int ticks) {
    driveTicks(pow, ticks, 30000);
  }

  boolean rotateDegs(double pow, double degs, int fallbackTicks) {
    double prevHeading = 366;
    boolean b = false;
    robot.resetTicks();
    do {
      robot.imu.update();
      robot.move(0, 0, ROTATE_SPEED * getDir());

//      telemetry.addData("degrees", robot.imu.heading());
//      telemetry.update();
//      if (prevHeading == robot.imu.heading()) {
//        b = true;
//        break;
//      }
//      prevHeading = robot.imu.heading();
    } while (Math.abs(robot.imu.heading()) < degs && opModeIsActive());

//    while (b && Math.abs(robot.getTicks()) < fallbackTicks && opModeIsActive()) {
//      robot.move(0, 0, pow);
//      telemetry.addData("ticks", robot.getTicks());
//      telemetry.update();
//    }

    robot.stopMotors();
    return !b;
  }
  boolean rotateDegs(double pow, double degs) {
    return rotateDegs(pow, degs, 0);
  }

  void shootParticles() throws InterruptedException {
    if (settings.numShots > 0) {
      fireParticle();
      sleep(SLEEP_TIME);
    }
    if (settings.numShots > 1) {
      transferParticle();
      sleep(SLEEP_TIME);
      fireParticle();
      sleep(SLEEP_TIME);
    }
  }

  public int pushButton (int color) throws InterruptedException {

    int redLeft, blueLeft;
    robot.colorSensor.enableLed(false);
    redLeft = robot.colorSensor.red();
    blueLeft = robot.colorSensor.blue();
    int hit = 0;

    sleep(250);
    if(redLeft*color > blueLeft*color) {
      robot.pressButton();
      hit = 1;
      sleep(500);
    }

    driveTicks(SPEED / 2, 300);
    if (hit==0) {
      sleep(500);

      redLeft = robot.colorSensor.red();
      blueLeft = robot.colorSensor.blue();
      sleep(250);

      if (redLeft * color > blueLeft * color) {
        robot.pressButton();
        hit = -1;
        sleep(500);
      }
    }
    return hit;
  }
  void alignWithLine() throws InterruptedException{
    while (!robot.isOnLinel()) {
      robot.moveRight(-SPEED);
    }
    robot.stopMotors();
    sleep(250);
    while (!robot.isOnLiner()) {
      robot.moveLeft(-SPEED);
    }
    robot.stopMotors();

  }
}
