/*-
Copyright 2018 Adobe. All rights reserved.
This file is licensed to you under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License. You may obtain a copy
of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
OF ANY KIND, either express or implied. See the License for the specific language
governing permissions and limitations under the License.
*/

package com.adobe.ride.sample;

import java.util.UUID;
import org.testng.annotations.Test;
import com.adobe.ride.core.types.ExpectedResponse;
import com.adobe.ride.sample.cloud_objects.ArrayObject;
import com.adobe.ride.sample.cloud_objects.ProfileObject;
import com.adobe.ride.sample.cloud_objects.SampleServiceObject1;
import com.adobe.ride.sample.cloud_objects.SampleServiceObject2;
import com.adobe.ride.sample.core.SampleServiceController;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.specification.ResponseSpecification;

/**
 * 
 * @author tedcasey
 *
 */
public class BasicTest_IT {

  @Test(groups = {"smoke", "acceptance"})
  public void testAuthenticatedCalltoServer() {
    String itemName = UUID.randomUUID().toString();
    // Create Object
    SampleServiceObject1 testObject = new SampleServiceObject1(itemName, false);

    // Send Object To Server. "true" here indicates that we want Ride to attempt to use the
    // CheckAuthFilter in the core and invoke any authentication library to attach a
    // valid credentials token.
    SampleServiceController.createOrUpdateObject(testObject.getObjectPath(), testObject,
        ExpectedResponse.CREATED_RESPONSE, true);
  }

  @Test(groups = {"smoke", "acceptance"})
  public void testValidateResponseAgainstSchema() {
    String itemName = UUID.randomUUID().toString();
    // Create Object
    SampleServiceObject1 testObject = new SampleServiceObject1(itemName, false);

    ResponseSpecBuilder response = new ResponseSpecBuilder();
    response.expectStatusCode(201);
    response.expectBody(JsonSchemaValidator.matchesJsonSchema(testObject.getModelString()));
    ResponseSpecification responseSpec = response.build();

    SampleServiceController.createOrUpdateObject(testObject.getObjectPath(), testObject,
        responseSpec, false);
  }

  @Test(groups = {"smoke", "acceptance"})
  public void runBasicArrayObjectTest() {
    String itemName = UUID.randomUUID().toString();
    ArrayObject testObject = new ArrayObject(itemName, false);

    ResponseSpecBuilder response = new ResponseSpecBuilder();
    response.expectStatusCode(201);

    ResponseSpecification expected = response.build();

    // Temporary boolean flag added to call to indicate that we want to authenticate
    // this call
    SampleServiceController.createOrUpdateArrayObject(testObject.getObjectPath(), testObject,
        expected, false);
  }

  @Test(groups = {"smoke", "acceptance"}, enabled = false)
  public void runSyncdObjectTest() {
    String itemName = UUID.randomUUID().toString();
    // Create Object
    SampleServiceObject2 testObject = new SampleServiceObject2(itemName, false);
    // Send Object To Server
    SampleServiceController.createOrUpdateObject(testObject.getObjectPath(), testObject,
        ExpectedResponse.CREATED_RESPONSE, false);
  }

  @Test(groups = {"smoke", "acceptance"})
  public void runComplexObjectTest() {
    String itemName = UUID.randomUUID().toString();
    // Create Object
    ProfileObject testObject = new ProfileObject(itemName, false);
    // Send Object To Server
    SampleServiceController.createOrUpdateArrayObject(testObject.getObjectPath(), testObject,
        ExpectedResponse.CREATED_RESPONSE, false);
  }

  @Test
  public void createObjUpdateAndDeleteObject() {
    // Create Object
    String itemName = UUID.randomUUID().toString();
    SampleServiceObject1 testObject = new SampleServiceObject1(itemName, false);
    SampleServiceController.createOrUpdate(testObject.getObjectPath(), testObject,
        ExpectedResponse.CREATED_RESPONSE);

    // Update Object
    testObject.setProperty("thisProperty", "fred");
    SampleServiceController.createOrUpdate(testObject.getObjectPath(), testObject,
        ExpectedResponse.OK_RESPONSE);

    // Delete Object
    SampleServiceController.delete(testObject.getObjectPath(), testObject,
        ExpectedResponse.DELETED_RESPONSE);
  }
}
