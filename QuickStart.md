![Quick Start](images/Ride_QuickStart.jpg)

* [Jump Start Your Engine](#jump-start-your-engine)
* [Dependencies](#dependencies)
* [Setup](#setup)
* [Resource Code](#resource-code)
* [JavaCode](#java-code)

# Jump Start Your Engine

There are a number of documents in this repo revolving around Ride usage and in depth solutions to planning your rest api testing needs.  But to get started using Ride, you should think of it simply as a sdk to help you build libraries to make testing your target REST APIs easier and more maintainable.  This document focuses on the bare basics of Ride and how to get up and running quickly. 

# Dependencies

You will need to add the Ride core dependency to your pom:
```
    <dependency>
      <groupId>com.adobe.ride</groupId>
      <artifactId>ride-core</artifactId>
      <version>1.0.2</version>
    </dependency>
```

# Setup

This quick start will focus on eclipse/maven build workflows, but ant and gradle and any other Java IDE should work just fine too.  To get started, create a new Maven project (sorry, Ride is java-only for now) in the IDE of your choice, giving it a groupId and artifactId you'd like to use (i.e. something like com.mycompany.mywebservice /  webservice-automation-library).  Add the following files to your project (don't worry about the contents, for now just create the place holders:

#### ResourceFiles
<html>
	<ul style="list-style-type:square">
		<li>src/main/resources</li>
		<ul style="list-style-type:square">            
			<li>schemas</li>
			<ul style="list-style-type:square">            
				<li>SampleService</li>
				<ul style="list-style-type:square">            
					<li>sample_service_object_1.json</li>
				</ul>
			</ul>
			<li>configs</li>
			<ul style="list-style-type:square">            
				<li>localhost.properties</li>
				<li>stage01.properties</li>
				<li>prod.properties</li>
			</ul>
		</ul>
	</ul>
</html>
		
#### Java Packages/Classes
<html>
	<ul style="list-style-type:square">
		<li>src/main/java</li>
		<ul style="list-style-type:square">            
			<li>com.mycompany.mywebservice.core</li>
			<ul style="list-style-type:square">            
				<li>MyWebServiceController.java</li>           
			<li>com.mycompany.mywebservice.objects</li>
			<ul style="list-style-type:square">            
				<li>RequestObject.java</li>
			</ul>
		</ul>
	</ul>
</html>
		
# Resource Code

## Schemas

Open request_object.json and populate it with the schema for your service call payload.  It should look something like this (in most cases, quite a lot more complex):
```
{
	"$schema": "http://json-schema.org/draft-06/schema",
	"title" : "sample service request object 1",
	"type": "object",
	"description": "sub_object",
	"required": ["name", "type"],
	"additionalProperties": false,
	"properties": {
		"type":  { "enum" : ["type1", "type2", "type3"]},
		"name": { "type" : "string", "pattern":"^[a-zA-z0-9]{3,30}"},
		"objectCode": { "type" : "integer", "minimum":1, "maximum":5},
		"thisProperty": { "type" : "string"}
	}
}
```


## Configs

Open the localhost.properties file and add the following code:
```
	declaredServices=SampleService
	isProduction=false
	
	#SampleService
	SampleService.schema=http
	SampleService.port=80
	SampleService.endpoint=localhost
	SampleService.basePath=/sample-service-server/rest
```

Open the stage.properties file and add the following code:

```
	declaredServices=SampleService
	isProduction=false
	
	# SampleService
	SampleService.schema=https
	SampleService.port=443
	SampleService.endpoint=www.mycompany.com
	SampleService.basePath=/sample-service-server/rest 
```
	
Alter the instances of "SampleService" to reflect the actual name of your service, and change your schema, ports, endpoints, and basepaths to the actual ones your service uses (if no basepath, just use /).  Note: whatever you name your service here, it should also match the name of the folder in the schemas resources.

Ok, your resources are setup.  Let's get your code set up.

# Java Code

Open the file RequestObject.java and add Code until it looks like the following:

```
package com.mycompany.mywebservice.objects;

import com.adobe.ride.utilities.model.ModelObject;

public class SampleServiceObject1 extends ModelObject {

  public SampleServiceObject1(String objectName, boolean initRequiredOnly) {
    super("SampleService", Service.SAMPLE_SERVICE.toString(), objectName, initRequiredOnly);
    buildValidModelInstance();
  }
}
```

What we have here is a simple java class which extends the ModelObject in Ride.  The superclass arguments tell Ride where the schema of the object and configs of the service live (based on what we've defined above).  The arguments we pass into the class tell it what we want to name the instance and whether we want to instantiate all of the properties defined in the json schema or just the required ones.

Now that we have an object, let's defined the controller which actually sends the object to the server.  Open the file MyWebServiceController.java and add Code until it looks like the following:
```
com.mycompany.mywebservice.core

import org.apache.http.HttpHeaders;
import com.adobe.ride.core.controllers.RestApiController;
import com.adobe.ride.core.types.MimeTypes;
import com.adobe.ride.utilities.model.ModelObject;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;

public class MyWebServiceController extends RestApiController {

  protected static RequestSpecBuilder getDefaultReqSpecBuilder(Filter... filters) {
    RequestSpecBuilder builder;
    builder = RestApiController.getRequestBuilder(true, filters);
    builder.addHeader(HttpHeaders.ACCEPT, MimeTypes.APP_JSON.toString());
    builder.addHeader(HttpHeaders.CONTENT_TYPE, MimeTypes.APP_JSON.toString());
    return builder;
  }

  public static Response firePOSTCall(String objectPath, ModelObject object,
      ResponseSpecification expectedResponse) {
    RequestSpecBuilder reqBuilder = getDefaultReqSpecBuilder();
    reqBuilder.setBody(object.getObjectMetadata());

    Response response = fireRestCall("SampleService", objectPath, reqBuilder,
          expectedResponse, method.POST);

    return response;
  }
}
```

Ok, now we have our config information setup and our basic object and controller code.  Let's compile it into a library.

From either your IDE or the location of your pom in terminal/command line window, run the standard maven install command:
``` mvn clean install ```

## Test Project and Code

Now we have a distributable library that you can post on your internal artifact repository (once you get it into a shape you like, for now we will use your locally compiled version).

Let's create a project to write our tests.  In your IDE create a new Java Project with a groupId and artifactId different from your library, something like com.mycompany.mywebservice.automation /  webservice-tests

In your pom, add a dependency for the library you just created:

```
    <dependency>
      <groupId>com.mycompany.mywebservice</groupId>
      <artifactId>webservice-automation-library</artifactId>
      <version>0.0.1-SNAPSHOT</version>
    </dependency>
```

Add the following package/class in the test source of your project

src/test/java
	- com.mycompany.mywebservice.tests
		- Basic\_Test\_IT.java
		
Open Basic\_Test\_IT.java and add code until it looks like the following:

```
import java.util.UUID;
import org.testng.annotations.Test;
import com.adobe.ride.core.types.ExpectedResponse;
import com.mycompany.mywebservice.core.MyWebServiceController;
import com.mycompany.mywebservice.objects.SampleServiceObject1;

public class BasicTest_IT {

  @Test(groups = {"smoke", "acceptance"})
  public void testCalltoServer() {
    String itemName = UUID.randomUUID().toString();
    // Create Object and dynamically generate data from schema
    SampleServiceObject1 testObject = new SampleServiceObject1(itemName, false);

    // Send Object To Server. 
    MyWebServiceController.firePOSTCall(testObject.getName, testObject,
        ExpectedResponse.CREATED_RESPONSE);
  }
}
```

Let's run the test, and specify which environment we want to target in the config definitions from the terminal:

``` mvn clean verify -Dtarget=stage01 ```

When it's run your REST call should be generated and logged, which you'll see in the console, and assuming your webservice exists in the way it is specified in the configs, you should see a response come back

## Final Thoughts

With a little bit of upfront work, you can realize an extremely useable and flexible library with which you can generate tests against your webservice in just a few lines of code.  Once you have an understanding of these quick start basics, you can begin to leverage all of the resources available in Ride to ensure your service is being fully tested.



