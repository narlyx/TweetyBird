<hr class="dashed">

![TweetyBirdLogo](https://github.com/itschesterlk/TweetyBird/assets/109835029/158b4edd-f69d-4380-9093-f34dd9e78b16)

![](https://img.shields.io/github/stars/itschesterlk/TweetyBird?color=yellow)
![](https://img.shields.io/github/release/itschesterlk/TweetyBird?color=yellow)
![GitHub commit activity (branch)](https://img.shields.io/github/commit-activity/t/itschesterlk/TweetyBird?color=yellow)

<hr class="dashes">

<h1 align="center">
	> TWEETYBIRD 2.X.X IS OUT, SEE INSTALLATION <
	<br>
	Will break prexisting TweetyBird usages...
</h1>
<br>
<br>

# What is TweetyBird
TweetyBird is a library that you can use on any FTC robot that meets the requirements below and takes the hassle out of programing a movement algorithm from scratch. Most teams spend most of their time trying to get their robot to move across the ground accuratly rather than programing what makes their robot unique. TweetyBird is also focused on being as simple to use and set up as we can make it.

# Note
Some day we hope that we can expand TweetyBird can expand past FTC and be able to be used on other robots that use a meccanum drivetrain with different kinds of odometry, but for now, we are sticking to FTC.

TweetyBird is still a newer project currently being made by a single programmer so bugs can occur, and we don't want that do we, so if you belive that the issue with movement is not your fault, feel free to submit an issue and we will take a look at it. If it is not our problem, we can gladly help!

Also, contributions of any kind are extreemly helpful. If you improve or fix anything, submit a pull request if you would like and you might just get some of your code in mainline TweetyBird!

# Requirements
* Four DC Motors controlling independent meccanum wheels
* 3 deadwheel encoders (one for each side, and one for the back)
* TweetyBird can only be used on the SDK's new LinearOpmode
* Useing an IDE (e.g. Android Studio) to program instead of block coding or onbotjava

# Installing TweetyBird
### Maven repository
*Coming Soon, will include a 3 line install and auto updates!*

### Manual Method
1. Download the latest release of TweetyBird's source code from <a href="https://github.com/itschesterlk/TweetyBird/releases">here!</a>, or if you are feeling a little risky and want to be on the bleeding edge, you can download the latest commit.<br><br>
2. Unzip the source code file and place the extracted file in your FTC Android SDK, this guide will use the root folder of the SDK. Also, keep note of the extracted file's name, you will need it later.<br><br>
3. Open your TeamCode build.gradle file (/TeamCode/build.gradle) and add the following line to the dependancies block: ```implementation project(path: ':TweetyBirdFileNameHere')``` . TweetyBirdFileNameHere should be replaced with your filename, if you did not modify it, it should go something along the lines of TweetyBird-x.x.x, where the x's represent the release number. (Android studio will autocomplete it for you most of the time)<br><br>
4. You are done! Give yourself a pat on the back for just how incredibly duificut that installation was!<br><br>

# Setting Up
1. **Importing TweetyBird into the LinearOpMode<br>**
	Head to any LinearOpMode you would like to use TweetyBird on and add ```import com.chesterlk.ftc.tweetybird.TweetyBirdProcessor;``` to the import section (the top of the file)
	This will import the TweetyBirdProcessor so that we can interface and use TweetyBird.<br><br>
2. **Creating TweetyBird variable (reconmended)<br>**
	Before or somewhere inside of your runOpMode add the line ```private TweetyBirdProcessor tweetyBird;```. Ofcource if you need private can be changed for public or whatever, and tweetyBird can be replaced with whatever you want, keep in mind, every time you use TweetyBird that TweetyBird instance, you will need to type it.<br><br>
3. **Building TweetyBird<br>**
	TweetyBird used the standardized Builder synctax, and as of current (2.0.0) all paramters available must be used. This is an example configuration, ofcourse change the values to suit your bot. (tweetyBird) is the variable name.<br>

	```
	tweetyBird = new TweetyBirdProcessor.Builder()
		//OpMode
			/*
			Passes on the LinearOpMode, we used "this" because it was ran within
			the opmode and java will pass the opmode on
			*/
		.setOpMode(this)
		
		//Physical Configuration
			//DC Motor Instance for Each Motor
		.setFrontLeftMotor(hwMap.frontLeft)
		.setFrontRightMotor(hwMap.frontRight)
		.setBackLeftMotor(hwMap.backLeft)
		.setBackRightMotor(hwMap.backRight)
		
		.setLeftEncoder(hwMap.leftEncoder)
		.setRightEncoder(hwMap.rightEncoder)
		.setBackEncoder(hwMap.backEncoder)
		
			/*
			Doubles of the distance from the center of rotation,
			to the center of the wheel in inches. (Elevation not counted)
			*/
		.setRadiusToLeftEncoder(7.625)
		.setRadiusToRightEncoder(7.625)
		.setRadiusToBackEncoder(7)
		
			//Encoder and encoder wheel details
		.setTicksPerEncoderRotation(8192)
		.setEncoderWheelRadius(1)
		
		//Setting Up Basic Configuration
		.setMinSpeed(0.15) //Minimum speed limit
		.setMaxSpeed(0.5) //Max Speed
		.setStartSpeed(0.17) //Minimum speed, only used when starting to move
		.setSpeedModifier(0.06) //Explained in javadoc's, speed = inches*speedModifier
		.setStopForceSpeed(0.1) //Motor power used to stop, too fast will skid, 0 is off
		
			//Correction
		.setCorrectionOverpowerDistance(5) //How far off until robot b-lines to correct
		.setDistanceBuffer(0.4) //How far the robot can be off in inches
		.setRotationBuffer(8) //How many degrees the robot is allowed to be off
		//Too small of a variable for the past three will cause jitters/rocking.
		
		//Building TweetyBird
		.build();
	```
	Each parameter has inline javadocs to guide you when setting up the parameters. And eventually a wiki/documentation page will be set up for TweetyBird.<br><br>
4. You are ready to use TweetyBird now, if you encounter the robot restarting without any error after installing TweetyBird, check to insure that the DC Motor instances exist and are initialized

# Usage
Now its time for the easy part, using TweetyBird. If your robot does some odd movements, check that your encoder wheels are making proper contact with the ground and your build parameters are accuate. If you are certian that it is not your falt, submit an issue to the GitHub page if noone else has, and we will get working on finding a fix or helping you.

By typing the variable name you assigned earlier followed by a dot, most IDE's will list the commands that you can use. Currently we do not have a wiki nor documentation so you will need to dig through the TweetyBirdProcessor file if you cannot see the autocompletion for commands in your ide. However for the time being, a few are listed below.

```tweetyBird.straightLineTo(x,y,z)```
This will make the robot move in a straight line to the specified coordinates, the grid starts with 0,0,0 (x,y,z) at the robots starting posotion and counts in inches, for example if you input 5,10,45 , the robot will move right 5 inches, forward 10 inches, and turn 45 degrees to the left from its starting position.

```tweetyBird.clearQueue()``` will clear the list of waypoints entered and stop

```tweetyBird.disengage()``` will stop TweetyBird from moving

```tweetyBird.engage()``` will allow TweetyBird to move after being stopped

```tweetyBird.waitWhileBusy()``` will hold the class you called it from until the robot stops moving

```tweetyBird.busy()``` returns if TweetyBird is moving or not (true if moving)

```tweetyBird.engaged()``` returns true if TweetyBird is engaged (allowed to control)


### Eventually the commands and parameters will recive proper documentation.
	