# Overview

The Adobe Ride Performance library is based on Scala and Gatling, but supports Java code, Ride core code, and libraries extended from the Adobe Ride framework.  Because of this difference in dependencies, there are a few IDE and command-line running setup steps that differ from Ride framework.  In terms of IDE setup, this document focuses on the Eclipse IDE.  However, at the time of the writing of this document, it was understood that IntelliJ had support already built in for Scala, so the setup may indeed be less with that IDE. 


## References:
* Performance Library Source: https://github.com/adobe/ride/tree/develop/libraries/cloud-automation-performance-lib
* Performance Sample code: https://github.com/adobe/ride/tree/develop/sample/sample-service-performance-tests
* Eclipse Scala-IDE plugin: http://scala-ide.org/ 
* SBT:  https://www.scala-sbt.org/1.0/docs/Setup.html 

## Requirements
* Java installed and $JAVA_HOME set
* Maven 3.2.* or higher installed and $M2, $M2_HOME set with $M2 added to $PATH variable
* SBT (Scala Build Tool) installed (similar to maven, gradle, or ant)
* Eclipse:
	Scala IDE for eclipse installed
	- Search for and install Scala IDE plugin from the marketplace view in the IDE (Help>Eclipse Marketplace...)

## Setting up your Environment:
### Maven
* If you are already setup to run Adobe Ride, then you most likely do not need any additional setup to run your performance tests through maven
	
### SBT
* In order to use SBT properly with eclipse you need to do a couple of things.
* First, go to the command-line and type "sbt about".
	 * if you have sbt installed properly, you should get version and plugin information.
* Once that works, go to /Users/<username>/.sbt/<version>/plugins/ , or in Windows C://Documents And Settings/Users/<username>/.sbt/<version>/plugins/
	 * If it doesn't exist, add a file called plugins.sbt.
	 * In that file add the following lines:
	 
	 	```
	 	addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")
		addSbtPlugin("io.gatling" % "gatling-sbt" % "2.2.2")
		```
	*  These lines tell SBT about gatling and eclipse.

	
## Setting up your project in Maven:
* From Eclipse: File>New>Scala Project
* If the project doesn't have an "S" on it's folder icon, right-click>Configure>Add Scala Nature
* Right Click on the project and create new source folder called src/main/scala
* Create a package (named whatever you like)
* Go to the sample-service-performance-tests sample project linked above, and copy the SampleServiceBasicRunner.scala class.
* In the package you created, paste the class.
* Rename the class to something that reflects the type of tests you are running.
* Go back to the sample-service-performance-tests project and copy the pom.xml file at the root of the project and paste it into the root of your new project.
* Change the groupId and artifactId in the pom to reflect your project.
* Remove the dependency on the sample-service-extension and add a dependency on your Adobe Ride extension.
* In the Build section, change the mainClass value to reflect the scala file you edited above.


## Setting up your project for SBT
* Run the first 7 steps above for setting up your project in Maven (steps are similar for sbt)
* Go back to the sample-service-performance-tests project and copy the build.sbt file at the root of the project and paste it into the root of your new project.
* Change the name of the project on line 1 to whatever the name of your project is.
* Remove line 11, and add dependencies for any Adobe Ride libraries you depend on for your tests in the SBT style that is illustrated in the other dependency.


## Modifying the Performance template for your code:
* Once you have your project setup and your first class renamed, open the class.
* Rename the package at the top of the class to the name of the package you created.
* Open the class the modify it as directed in the code comments numbered steps 1, 2, and 3.
	* Please note that naming is important with modifications here.  Without going into too much detail, a Scala "object" type (as the runner is) is a singleton, and you may run into collisions if you have multiple tests that are not named uniquely.
* Open the build.sbt file you copied over
	 

## Running your performance tests from Eclipse Through Scala directly:
* If you have set up your test appropriately, when you right-click the tests class, and select Run (or debug) you should see an option to run as "Scala Application"
* The first time you run, it will fail because you haven't yet set a target.
* Right click your test class again and select Run>Run Configurations...
* In the config dialog, click on the "Arguments" tab and in the "VM Arguments" dialog enter any target environment you have configured in your Adobe Ride code, ex: -Dtarget=stage, or -Dtarget=production
* Ensure that you have the necessary environment variables defined.  As with any other Ride supported tests, these variables need to be in place in addition to any authentication/authorization process you may have implemented in your CheckAuthUtilFilter (See Adobe Ride Usage Docs).
* Click apply and run.
* Your tests should run and in the console log, you will see results as well as a link to the html charts that gatling has created for you.

##Running your performance test from Eclipse through Maven:
* Right click your project and select Run as>Maven Build
* In the goals, type: ``` scala:compile scala:run```
* In the JRE tab, place a Ride target environment flag like ```-Dtarget=stage``
* Click run.

## Running your performance tests from the command-line/terminal with Maven:
* From the command-line/terminal, navigate to the root of your project.
* Type ```mvn scala:compile scala:run -Dtarget=stage``
	
## Running your performance tests from the command-line/terminal with SBT:
* From the command-line/terminal, navigate to the root of your project.
* Assuming you have sbt setup correctly, type "sbt".
* Open the sbt interface (you simply prepend the following commands into the terminal with "sbt" to achieve the same results without entering the terminal), and type the following commands (the only critical one is the last, but to be more illustrative, all of these are listed)
	* clean
	*  update
	* compile
	*  run
* After this last command, your tests should run just as they do in Eclipse.
* If you want to run a specific testclass, the command looks like this:

	```
	sbt "run-main <path>.<for>.<the>.<package>.<TestClass>" -Dtarget=<targetConfigName>
	```
	 
## Additional Info
* Because SBT relies on ivy for it's library management, you may find it necessary to clear out the cache from timt to time, just like you have to do with Maven.
* Similar to Maven, the ivy cache is located in /Users/<username>/.ivy2
	

Contact: tedcasey@adobe.com 
