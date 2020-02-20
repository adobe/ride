![Adobe Ride Logo](images/RideBanner.png)

## Overview

Adobe Ride is a REST API test automation framework written in Java and built on top of industry-standard tools such as Rest-Assured, TestNG, and Gatling.  The library is service-agnostic, modular and extensible.  The goal of the framework is to speed the development of tests and validation of APIs by abstracting repetitive code and standardizing calls for easier functional and end-to-end testing across micro-services.

The use of Ride is completed by extending the framework, providing that extension with config information, in the form of environment values and schemas, and then writing tests using the configured extension(s).

## Current

## Getting Started

To begin to building your test extention, you will need to start by creating a standard java project.  The simplest way to do this in Eclipse or IntelliJ is to create a new Maven project (Maven is the build driver discussed and used in the samples).

	 ### Configuration

	 With your project in place, you'll need to start by telling Ride how to talk to your service (the config info discussed above). Your API definitions (payloads / environment configs) are defined in your project resources (sometimes contained in a separate jar for sharing).  They should be placed in folders named as shown below:

	 <img src="images/configs_schemas.png" width="351px"/>
<br>
<br>

#### Configs (Environment)

Ride is designed so that more that multiple environments can be defined, and multiple services for each environment can be defined.  In the example below, 2 services are defined for *localhost*___.properties___ (how to target them is discussed a bit further down).  They will be consumed by your extension:

<img src="images/localhost.properties.png" width="414px"/>
<br>
<br>

#### Schemas (Payload)

Your service payloads are defined by standard json schema, and are organized into the services they target by folder as shown above.

### Extending

Once you have your schemas and configs setup, you can begin to create your extension, which is done by [extending](https://github.com/adobe/ride/blob/develop/sample/sample-service-extension/src/main/java/com/adobe/ride/sample/cloud_objects/SampleServiceUberObject.java#L32) the [ModelObject](https://github.com/adobe/ride/blob/develop/utilities/ride-model-util/src/main/java/com/adobe/ride/utilities/model/ModelObject.java)  and [extending](https://github.com/adobe/ride/blob/develop/sample/sample-service-extension/src/main/java/com/adobe/ride/sample/core/SampleServiceController.java#L33) the [RestApiController](https://github.com/adobe/ride/blob/develop/core/src/main/java/com/adobe/ride/core/controllers/RestApiController.java)

### Writing Tests



### Fuzzing