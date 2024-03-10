<hr class="dashed">

<div align="center">

<br>

<img src="https://github.com/narlyx/TweetyBird/assets/109835029/aab1e7b5-45cb-4179-b6c8-14f828e2d562">

<br>

<img src="https://img.shields.io/github/stars/narlyx/TweetyBird?color=yellow">
<img src="https://img.shields.io/github/release/narlyx/TweetyBird?color=yellow">
<img src="https://img.shields.io/github/commit-activity/t/narlyx/TweetyBird?color=yellow">

<br>

•
[About](#what-is-tweetybird) •
[Requirements](#requirements) •
[Installation](#installation) •

</div>

<br>

# What is TweetyBird
TweetyBird is a library that you can use on any FTC robot that meets the requirements below and takes the hassle out of programing a movement algorithm from scratch. Most teams spend most of their time trying to get their robot to move across the ground accuratly rather than programing what makes their robot unique. TweetyBird is also focused on being as simple to use and set up as we can make it.

# Requirements
Although TweetyBird is very versatile, there are still some requirements that you will need:
* Using an IDE (e.g. Android Studio) to program instead of block coding or onbotjava
* A mecanum drivetrain with 4 individual DC motors
* 3 deadwheels (drop down odometry pods) set up with two vertically, and one horizontal
* OpModes using the newer "LinearOpMode"

### Quick Note
TweetyBird was developed and tested on Android Studio, so I highly recommend using <a href="https://developer.android.com/studio">Android Studio</a> if you experience any issues with setting up and using TweetyBird.

# Installation
As of current there are two methods to install TweetyBird, you can install it either though Github Packages, or by cloning the repository and locally referencing it.

### Installing TweetyBird via Github Packages
<details>
<summary>Click here</summary>

### • Step 1 - Creating a Github token (If you have alerady added a token to your machines path, proceed to the next step.)
The to your classic tokens page on Github:
Your Profile Picture > Settings > Developer Settings at the bottom of the side var > Personal Access Tokens.

Or you may just follow this link: <a href="https://github.com/settings/tokens">https://github.com/settings/tokens</a>

Next create a Classic Token: Generate New Token > Generate New Token (classic)

Give the token a note describing its use case, for example, you add your machines hostname, this does not need to be anything specific, just a reminder.

You can either set the expiration date to never, or to a set time, but whenever the token expires, you will need to update it in your path with a brand new token. Also keep in mind, you NEVER want to share this token with ANYONE as it gives access direcrtly to your account.

If you just plan on using Github Packages (or downloading TweetyBird) you only need to give this token the "read:packages" premission.

Then press generate token and SAVE that token as we are going to need it in the next step, this is also the last time Github will tell you this token.

### • Step 2 - Adding you Github Token to your path
This is different for most operating systems, I will genericly list how to do this on Windows, if your are using something Unix based or anything else, search up how to add a variable to your path/enviornmet for your OS.

You will need to set a USERNAME variable (your Github username) and a TOKEN variable (Your newly generated token/preexisting token) These variables can be named whatever you want, but you will need to add the same exact name in your SDK

On windows, open your start menu, and start typing/search "Enviornment Variables" and then open the first result, it should be a Windows application with a name something along the lines as "Edit system enviornment variables"

Under your user varibles, add two new variables being the ones mentioned above.

### • Step 3 - Adding the repository to your project
Edit the "build.dependancies.gradle" file within the projects root directory and add the following lines within the "repositories {}" block:

```
maven {
	url = "https://maven.pkg.github.com/narlyx/TweetyBird"
	credentials {
		username = project.findProperty("gpr.user")
		password = project.findProperty("gpr.key")
	}
}
```

Then add `implementation 'dev.narlyx.ftc:tweetybird:x.x.x'` in the "dependancies {}" block, also make sure you replace the 'x.x.x' with the latest release on Github, as of the time of writing this, the latest release would look like this: `implementation 'dev.narlyx.ftc:tweetybird:3.0.0'`

Whenever you want to update to a newer version, replace the numbers according to Github.

[Head to the final step](#final-step-this-is-for-all-methods-of-installation)

</details>

### Installing TweetyBird Locally (Currently the Easiest Method)
<details>
<summary>Click here</summary>

### • Step 1 - Downloading
First you will need to clone/download this repository, I highly reconmend you do this though Git (it doesnt matter what tool you use), that way you can fetch and pull from the orgin whenever there is a update.

Whenever you plan to update TweetyBird, either repull/reclone the project or redownload it and replace the old folder.

### • Step 2 - Planting the folder
Place the downloaded folder into the root directory of your SDK (alongside FtcRobotController and TeamCode). To make things easier, rename the downloaded folder to "TweetyBird"

### • Step 3 - Editing gradle files to read the repository
You are going to have to edit a few gradle files that the SDK uses so that TweetyBird can be read.

The first file to edit is the "settings.gradle" file within the projects root directory to read the file. At the bottom of the file you are going to want to add `include ':TweetyBird'`, replace TweetyBird with what you named the file.

Then we are going to edit the "build.dependancies.gradle" file also within the projects root directory to implement TweetyBird. At the bottom of the "dependancies {}" block add `implementation project(path: ':TweetyBird')`.

[Head to the final step](#final-step-this-is-for-all-methods-of-installation)
</details>

### Final step (this is for all methods of installation)
Now you will need to sync the gradle files and it will build TweetyBird for you, if you are using Android studio, you will have already got the sync gradle files notification multiple times, and you can click sync from there.

<br>

# Setup
TweetyBird uses the Java builder format to initialize and start TweetyBird.

### Import
First of all, head the to the class where you want to set up TweetyBird and import the `TweetyBirdProcessor` class, it should be added along with all the other imports and look like this: `import dev.narlyx.ftc.tweetybird.TweetyBirdProcessor;`

### Definition
Next, its typically good practice to define a TweetyBird variable before we set it, add this line wherever you would like in your class `public TweetyBirdProcessor tweetyBird;`. This can be made public/private/etc based on your use cases.

### Builder
Next we are going to run the builder class for TweetyBird, this will initialze your configuration and then start TweetyBird.

The basic stucture will look like this
```
tweetyBird = new TweetyBirdProcessor.Builder()
	.exampleConfiguration(1)
	.exampleConfiguration(2) // The examples are not real options
	.build();
```
it is basically initializing your variable, and then calling the builder method, and once all your confinguration is done, you call the build method/option to start TweetyBird and end the statement.

### Configuration Options
You can also take at peek at example programs to learn how to setup/use TweetyBird there (coming soon)
<details>
<summary>Click to expand</summary>

#### Required Configurations
<details>
<summary>setOpMode(LinearOpMode)</summary>
Pass in your current opmode, see examples if you are unsure
</details>
<br>
<details>
<summary>setFrontLeftMotor(DCMotor)</summary>
Pass in your front left motor variable
</details>
<br>
<details>
<summary>setFrontRightMotor(DCMotor)</summary>
Pass in your front right motor variable
</details>
<br>
<details>
<summary>setBackLeftMotor(DCMotor)</summary>
Pass in your back left motor variable
</details>
<br>
<details>
<summary>setBackRightMotor(DCMotor)</summary>
Pass in your back right motor variable
</details>
<br>
<details>
<summary>setLeftEncoder(DCMotor)</summary>
Pass in your left encoder "motor" variable
</details>
<br>
<details>
<summary>setRightEncoder(DCMotor)</summary>
Pass in your right encoder "motor" variable
</details>
<br>
<details>
<summary>setMiddleEncoder(DCMotor)</summary>
Pass in your middle encoder "motor" variable
</details>
<br>
<details>
<summary>setSideEncoderDistance(double)</summary>
Distance between the two side encoders in inches
</details>
<br>
<details>
<summary>setMiddleEncoderOffset(double)</summary>
Distance in inches of how far forward/backwards the middle encoder is from the two side encoders

Currently this is "backwards", forward offset is negitive, backwards offset is positive, this is subject to change
</details>
<br>
<details>
<summary>setTicksPerEncoderRotation(double)</summary>
Ammount of times the encoder will count/tick for a full rotation (look this up for your specific encoder)
</details>
<br>
<details>
<summary>setEncoderWheelRadius(double)</summary>
The radius of the wheel attached to your encoder
</details>

#### Optional Configurations
<details>
<summary>flipLeftEncoder(boolean)</summary>
All encoders must be reading BACKWARDS when moving the bot FORWARD or to the RIGHT

If you are unable to flip the encoder direction though hardware setup then you can flip it here
</details>
<br>
<details>
<summary>flipRightEncoder(boolean)</summary>
All encoders must be reading BACKWARDS when moving the bot FORWARD or to the RIGHT

If you are unable to flip the encoder direction though hardware setup then you can flip it here
</details>
<br>
<details>
<summary>flipMiddleEncoder(boolean)</summary>
All encoders must be reading BACKWARDS when moving the bot FORWARD or to the RIGHT

If you are unable to flip the encoder direction though hardware setup then you can flip it here
</details>
<br>
<details>
<summary>setMinSpeed(double)</summary>
Set the minimum speed TweetyBird can go from 0-1, the default is 0.25
</details>
<br>
<details>
<summary>setMaxSpeed(double)</summary>
Set the maximum speed TweetyBird can go from 0-1, the default is 0.8
</details>
<br>
<details>
<summary>setStartSpeed(double)</summary>
Set the start speed (minim speed but for starting from a stop) TweetyBird can go from 0-1, the default is 0.4
</details>
<br>
<details>
<summary>setStopForceSpeed(double)</summary>
Set the speed used to try and keep the robot in place, the default is 0
</details>
<br>
<details>
<summary>setSpeedModifier(double)</summary>
A very fine variabe used to control accelration, only change by tiny ammounts at a time, the default is 0.04
</details>
<br>
<details>
<summary>setCorrectionOverpowerDistance(double)</summary>
How far in inches does TweetyBird need to be off from its path to "B-Line" it straight back to its path in inches, the default is 5
</details>
<br>
<details>
<summary>setDistanceBuffer(double)</summary>
How far TweetyBird can be off of its target position in inches (to low of a value will cause TweetyBird to rock back and fourth), the default is 1
</details>
<br>
<details>
<summary>setRotationBuffer(double)</summary>
How far TweetyBird can be off of its target rotation in degrees (to low of a value will cause TweetyBird to spin back and fourth), the default is 8
</details>
<br>
</details>