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

import com.adobe.ride.core.globals.CoreGlobals;
import com.adobe.ride.core.types.ExpectedResponseTypes;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

/**
 * 
 * @author tedcasey
 *
 */
public class Base {
  // Base singleton
  public static final Base INSTANCE = new Base();

  public final ResponseSpecification OK_RESPONSE =
      ExpectedResponseTypes.OK.getResponseSpecification();
  public final ResponseSpecification DELETED_RESPONSE =
      ExpectedResponseTypes.DELETED.getResponseSpecification();
  public final ResponseSpecification CREATED_RESPONSE =
      ExpectedResponseTypes.CREATED.getResponseSpecification();
  public final ResponseSpecification NOT_FOUND_RESPONSE =
      ExpectedResponseTypes.NOT_FOUND.getResponseSpecification();
  public final ResponseSpecification NOT_AUTH_RESPONSE =
      ExpectedResponseTypes.UNAUTHORIZED.getResponseSpecification();
  public final ResponseSpecification BAD_REQ_RESPONSE =
      ExpectedResponseTypes.BAD_REQUEST.getResponseSpecification();
  public final ResponseSpecification CONFLICT_RESPONSE =
      ExpectedResponseTypes.CONFLICT.getResponseSpecification();
  public final ResponseSpecification FORBIDDEN_RESPONSE =
      ExpectedResponseTypes.FORBIDDEN.getResponseSpecification();
  public final ResponseSpecification ACCEPTED_RESPONSE =
      ExpectedResponseTypes.ACCEPTED.getResponseSpecification();
  public ResponseSpecification NO_RESPONSE = new ResponseSpecBuilder().build();

  public String environment;
  public final String QE01_TARGET = "qe01";
  public final String STAGE01_TARGET = "stage01";
  public final String PROD01_TARGET = "prod01";

  public Base() {
    CoreGlobals.INSTANCE.setAPIKey("Sample");
    environment = CoreGlobals.INSTANCE.getTarget();
    /*-
    System.out.println("ENVIRONMENT: " + environment);
    // setup static reusable object
    if (environment.equals(PROD01_TARGET)) {
      // Do Something special
    } else if (environment.equals(STAGE01_TARGET)) {
      // Do Something different
    } else {
      // And now for something completely different
    }
    */
  }

}
