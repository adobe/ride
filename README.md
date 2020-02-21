![Adobe Ride Logo](images/RideBanner.png)

# Overview

Adobe Ride is a REST API test automation framework written in Java and built on top of industry-standard tools such as Rest-Assured, TestNG, and Gatling.  The library is service-agnostic, modular and extensible.  The goal of the framework is to speed the development of tests and validation of APIs by abstracting repetitive code and standardizing calls for easier functional and end-to-end testing across micro-services.

The use of Ride is completed by extending the framework, providing that extension with config information, in the form of environment values and schemas, and then writing tests using the configured extension(s).

# Current

<table>
    <tr>
        <th>Modules</th>
        <th>Version</th>
    </tr>
    <tr>
        <td>all</td>
        <td>2.0.0</td>
    </tr>
</table>


# Getting Started

To begin to building your test extention, you will need to start by creating a standard java project.  The simplest way to do this is to create a new Java project in your IDE/Build Tool of chiode (The samples shown here are in Eclipse using Maven).

## Configuration

With your project in place, you'll need to start by telling Ride how to talk to your service (the config info discussed above). Your API definitions (payloads / environment configs) are defined in your project resources (sometimes contained in a separate jar for sharing).  They should be placed in folders named as shown below:

<img src="images/configs_schemas.png" width="351px"/>
<br>
<br>

### Configs (Environment)

Ride is designed so that more that multiple environments can be defined, and multiple services for each environment can be defined.  In the example below, 2 services are defined for *localhost*___.properties___ (how to target them is discussed a bit further down).  They will be consumed by your extension:

<img src="images/localhost.properties.png" width="414px"/>
<br>
<br>

### Schemas (Payload)

Your service payloads are defined by standard json schema, and are organized into the services they target by folder as shown above.

## Extending

Once you have your schemas and configs setup, you can begin to create your extension, which is done by [extending](https://github.com/adobe/ride/blob/develop/sample/sample-service-extension/src/main/java/com/adobe/ride/sample/cloud_objects/SampleServiceUberObject.java#L32) the [ModelObject](https://github.com/adobe/ride/blob/develop/utilities/ride-model-util/src/main/java/com/adobe/ride/utilities/model/ModelObject.java)  and [extending](https://github.com/adobe/ride/blob/develop/sample/sample-service-extension/src/main/java/com/adobe/ride/sample/core/SampleServiceController.java#L33) the [RestApiController](https://github.com/adobe/ride/blob/develop/core/src/main/java/com/adobe/ride/core/controllers/RestApiController.java)

An examination of the pom files in those two sample projects will given you an idea of the dependencies you will need to identify in order to build and use your extension.

## Writing Tests


Once you have created and built your extension, you can begin using is tests which abstract away a significant portion repetitive code, as is shown below:

```
  @Test(groups = {"smoke", "acceptance"})
  public void testAuthenticatedCalltoServer() {
    String itemName = UUID.randomUUID().toString();

    // Create  Object
    SampleServiceObject1 testObject = new SampleServiceObject1(itemName, false);
    
    // 
    SampleServiceController.createOrUpdateObject(testObject.getObjectPath(), testObject,
        ExpectedResponse.CREATED_RESPONSE, true);
  }
```

## Fuzzing

With a few simple lines of code, you can generate many negative tests against the JSON payload sent to your service.  Ride internally maintains arrays of non-strings, SQL injection strings, No SQL injection strings, and localized strings to teste against the definitions in your schema.  This fuzzing walks the entire hiearchy of any arbitrary schema and tests each node in isolation ([sample code](https://github.com/adobe/ride/blob/develop/sample/sample-service-tests/src/test/java/com/adobe/ride/sample/Basic_FuzzTest_IT.java)):

```
@Factory
  public Object[] fuzzObjectMetadata_IT() throws Exception {
    String itemName = UUID.randomUUID().toString();
    SampleServiceObject1 object1 = new SampleServiceObject1(itemName, false);
    return new Object[] {new MetadataFuzzer(Service.SAMPLE_SERVICE.toString(), object1)};
  }


```

## Performance Testing

Part of the power of Ride is that once you have your extension written, you can use it for fuzzing (as shown above), but also for performance testing.  The performance library in Ride is based on Gatling, so while there is a bit of setup, and a bit of a shift in syntax to scala, you are still able to setup tests that measure performance for full flows of data withough having to rewrite what you've already created in your extension - [sample Ride performance test](https://github.com/adobe/ride/blob/develop/sample/sample-service-performance-tests/src/main/scala/com/adobe/ride/sample/performance/SampleServiceBasicRunner.scala)


## Additional Documentation

There are a number of supporting Informational documents in this repo which will help you utilize and perhaps contribute to Ride.


* [Architecture (Former Main Readme)](https://github.com/adobe/ride/blob/develop/Architecture.md)
* [Quick Start](https://github.com/adobe/ride/blob/develop/QuickStart.md)
* [Usage](https://github.com/adobe/ride/blob/develop/Usage.md)
* [Using Authentication](https://github.com/adobe/ride/blob/develop/UsingAuthentiation.md)


