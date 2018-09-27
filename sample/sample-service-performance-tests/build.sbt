name := "sample-service-performance-tests"

version := "1.0"

scalaVersion := "2.12.6"

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
 "com.adobe.ride.libraries" % "ride-performance-lib" % "0.0.1-SNAPSHOT",
 "com.adobe.ride.sample" % "sample-service-extension" % "0.0.1-SNAPSHOT"
)