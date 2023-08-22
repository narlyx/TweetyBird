package com.chesterlk.ftc.tweetybird.util;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

public class TB_DrivetrainHwmap {
    //Define Hardware Map
    HardwareMap hwMap = null;

    //Define Motors
    public DcMotor frontLeft  = null;
    public DcMotor frontRight = null;
    public DcMotor backLeft   = null;
    public DcMotor backRight  = null;

    //Defining Dead Wheels
    public DcMotor leftEncoder, rightEncoder, backEncoder;

    //IMU
    public BNO055IMU imu = null;


    public void init(OpMode opMode) {
        //Setting Hardware Map
        hwMap = opMode.hardwareMap;

        //Init Motors
        frontLeft = hwMap.get(DcMotor.class, "FL");
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontLeft.setDirection(DcMotor.Direction.FORWARD);

        frontRight = hwMap.get(DcMotor.class, "FR");
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        frontRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setDirection(DcMotor.Direction.REVERSE);

        backLeft = hwMap.get(DcMotor.class, "BL");
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        backLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeft.setDirection(DcMotor.Direction.REVERSE);

        backRight = hwMap.get(DcMotor.class, "BR");
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        backRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRight.setDirection(DcMotor.Direction.FORWARD);

        //Init Dead Wheels
        leftEncoder = hwMap.get(DcMotor.class, "left");
        leftEncoder.setDirection(DcMotorSimple.Direction.REVERSE);

        rightEncoder = hwMap.get(DcMotor.class, "right");
        rightEncoder.setDirection(DcMotorSimple.Direction.FORWARD);

        backEncoder = hwMap.get(DcMotor.class, "back");
        backEncoder.setDirection(DcMotorSimple.Direction.FORWARD);

        //Init IMU
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json";
        parameters.loggingEnabled = true;
        parameters.loggingTag = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();
        imu = hwMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);
        imu.startAccelerationIntegration(new Position(), new Velocity(), 1000);
    }

    //Return IMU Values
    public double getZ() {
        return (imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle);
    }
}
