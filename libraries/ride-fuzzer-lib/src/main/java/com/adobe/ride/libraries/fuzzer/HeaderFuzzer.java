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

package com.adobe.ride.libraries.fuzzer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.adobe.ride.core.controllers.RestApiController;
import com.adobe.ride.core.globals.Headers;
import com.adobe.ride.libraries.fuzzer.engines.CoreEngine;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

/**
 * Class for fuzzing headers
 * 
 * @author tedcasey
 * 
 *         Work in Progress
 * 
 */
public class HeaderFuzzer extends CoreEngine {
  protected String callingService;
  protected String path;
  protected Method RESTmethod;
  private RequestSpecification requestSpec;
  private String header;

  /**
   * Constructor to be used when fuzzing a header.
   * 
   * @param reqSpec RequestSpecBuilder to be used in the call.
   * @param path Path to be used in the call.
   * @param headerToBeFuzzed Specific header to be targeted for fuzzing.
   * @param method Method to be used.
   * @throws Exception
   */
  public HeaderFuzzer(String callingService, RequestSpecBuilder reqSpec, String path,
      Headers headerToBeFuzzed, Method method) throws Exception {
    super("header", headerToBeFuzzed.toString());
    this.callingService = callingService;
    requestSpec = reqSpec.build();
    RESTmethod = method;
    header = headerToBeFuzzed.toString();
    this.path = path;
  }

  /**
   * Internal method to define the RequestSpecBuilder with the fuzzed header.
   * 
   * @param value fuzz Value.
   * @return
   * @throws UnsupportedEncodingException
   */
  private RequestSpecBuilder defineRequest(Object value) {
    RequestSpecBuilder bldr = new RequestSpecBuilder();
    bldr.addRequestSpecification(requestSpec);
    try {
      bldr.addHeader(header, URLEncoder.encode(value.toString(), "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      System.out.println("FUZZER ERROR - UnsupportedEncodingException: " + e.getMessage());
      e.printStackTrace();
    }

    return bldr;
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param scope
   * @param header
   * @param value
   * @throws UnsupportedEncodingException
   */
  @Test(dataProvider = "nonStringsDP", groups = {"certification"}, enabled = false)
  public void fuzzHeaderWithNonStrings(String scope, String header, Object value) {
    fireREST(defineRequest(value));
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param scope
   * @param header
   * @param value
   */
  @Test(dataProvider = "localizedStringsDP", suiteName = "fuzzer", groups = "certification")
  public void fuzzHeaderWithLocalizedStrings(String scope, String header, Object value) {
    fireREST(defineRequest(value));
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param scope
   * @param header
   * @param value
   */
  @Test(dataProvider = "passiveSqlDP", suiteName = "fuzzer", groups = "certification")
  public void fuzzHeaderWithPassiveSQLInjectionStrings(String scope, String header, Object value) {
    fireREST(defineRequest(value));
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param scope
   * @param header
   * @param value
   */
  @Test(dataProvider = "sqlDP", suiteName = "fuzzer", groups = "certification")
  public void fuzzHeaderWithSQLInjectionStrings(String scope, String header, Object value) {
    fireREST(defineRequest(value));
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param scope
   * @param header
   * @param value
   */
  @Test(dataProvider = "noSqlDP", suiteName = "fuzzer", groups = "certification")
  public void fuzzHeaderWithNoSQLInjectionStrings(String scope, String header, Object value) {
    fireREST(defineRequest(value));
  }

  /**
   * Method to validate a 4xx error responses for expected failures and 2xx expected successes.
   * 
   * @param response
   * 
   */
  @Override
  public void validateResult(Response response, boolean expectSuccess) {
    int code = response.getStatusCode();
    if (expectSuccess) {
      Assert.assertTrue(199 < code && code < 299);
    } else if (header.equals(Headers.CLIENT_API_KEY.toString())) {
      Assert.assertEquals(code, 403);
    } else {
      Assert.assertEquals(code, 400);
    }
  }

  /**
   * Internal method to fire the REST call with the fuzzed header.
   * 
   * @param bldr
   */
  private void fireREST(RequestSpecBuilder bldr) {
    ResponseSpecBuilder expectedValues = new ResponseSpecBuilder();
    // expectedValues.expectBody(JsonSchemaValidator.matchesJsonSchema(schema));

    ResponseSpecification expectedResponse = expectedValues.build();
    Response response =
        RestApiController.callRestAPI(callingService, path, bldr, expectedResponse, RESTmethod);

    validateResult(response, false);
  }

}
