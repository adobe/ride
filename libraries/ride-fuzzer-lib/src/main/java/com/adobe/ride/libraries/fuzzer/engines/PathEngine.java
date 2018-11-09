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

package com.adobe.ride.libraries.fuzzer.engines;


import org.testng.Assert;
import org.testng.annotations.Test;

import com.adobe.ride.core.controllers.RestApiController;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.http.Method;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;

/**
 * Engine used to fuzz a path.
 * 
 * Work in Progress
 *
 * @author tedcasey
 * 
 */
public class PathEngine extends CoreEngine {
  protected String serviceName;
  private Method httpMethod;
  private RequestSpecBuilder reqSpecBldr;
  private Filter[] filters;

  /**
   * Constructor
   * 
   * @param reqSpec RequestSpecBuilder to be used in the call to the core controller. Usually these
   *        are control values, not test values.
   * @param path String path to be fuzzed.
   * @param target String part of the target to be fuzzed
   * @param rESTmethod HTTP method call to be used.
   */
  public PathEngine(String serviceName, RequestSpecBuilder reqSpec, String path, String target,
      Method RESTmethod, Filter... filters) {
    super(path, target);
    this.serviceName = serviceName;
    this.filters = filters;
    httpMethod = RESTmethod;
  }

  /**
   * Method to validate a 4xx error responses for expected failures and 2xx expected successes.
   * 
   * @param response
   * @override
   */
  public void validateResult(Response response, boolean expectSuccess) {
    int code = response.getStatusCode();
    if (expectSuccess) {
      Assert.assertTrue(199 < code && code < 299);
    } else {
      Assert.assertTrue(399 < code && code < 499);
    }
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param path
   * @param pathTarget
   * @param value
   */
  @Test(dataProvider = "nonStringsDP", suiteName = "fuzzer", groups = "certification",
      singleThreaded = true)
  public void fuzzPathWithNonStrings(String path, String pathTarget, Object value) {
    String testPath = path.replace(pathTarget, value.toString());
    fireREST(testPath);
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param path
   * @param pathTarget
   * @param value]
   */
  @Test(dataProvider = "localizedStringsDP", suiteName = "fuzzer", groups = "certification",
      singleThreaded = true)
  public void fuzzPathWithLocalizedStrings(String path, String pathTarget, Object value) {
    String testPath = path.replace(pathTarget, value.toString());
    fireREST(testPath);
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param path
   * @param pathTarget
   * @param value
   */
  @Test(dataProvider = "passiveSqlDP", suiteName = "fuzzer", groups = "certification",
      singleThreaded = true)
  public void fuzzPathWithPassiveSQLInjectionStrings(String path, String pathTarget, Object value) {
    String testPath = path.replace(pathTarget, value.toString());
    fireREST(testPath);
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param path
   * @param pathTarget
   * @param value
   */
  @Test(dataProvider = "sqlDP", suiteName = "fuzzer", groups = "certification",
      singleThreaded = true)
  public void fuzzPathWithSQLInjectionStrings(String path, String pathTarget, Object value) {
    String testPath = path.replace(pathTarget, value.toString());
    fireREST(testPath);
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param path
   * @param pathTarget
   * @param value
   */
  @Test(dataProvider = "noSqlDP", suiteName = "fuzzer", groups = "certification",
      singleThreaded = true)
  public void fuzzPathWithNoSQLInjectionStrings(String path, String pathTarget, Object value) {
    String testPath = path.replace(pathTarget, value.toString());
    fireREST(testPath);
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target. Call used to send fuzzed
   * path to service.
   * 
   * @param path Fuzzed path that will be sent.
   */
  private void fireREST(String path) {

    ResponseSpecBuilder expectedValues = new ResponseSpecBuilder();
    expectedValues.expectBody(JsonSchemaValidator.matchesJsonSchema(errorSchema));

    ResponseSpecification expectedResponse = expectedValues.build();
    Response response = RestApiController.fireRestCall(serviceName, path, reqSpecBldr, expectedResponse, httpMethod, filters);

    validateResult(response, false);
  }
}
