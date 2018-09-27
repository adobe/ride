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

package com.adobe.ride.core.controllers;

import com.adobe.ride.core.globals.Headers;
import com.adobe.ride.core.globals.Headers.HeaderItem;
import com.adobe.ride.core.types.ExpectedResponse;
import com.adobe.ride.utilities.model.ModelObject;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;

/**
 * 
 * @author tedcasey
 *
 */
public class BoilerPlateRestController extends RestApiController {


  private static String DEFAULT_HEADER_VALUE = Headers.JSON_CONTENT_TYPE;
  private static String UNSUPPORTED_HEADER_VALUE = Headers.XML_CONTENT_TYPE;
  private static String INVALID_HEADER_VALUE = "Invalid Value";

  /**
   * Two supported header test types.
   */
  public enum TestType {
    UNSUPPORTED, INVALID;
  }

  public static void setDefaultHeaderValue(String headerValue) {
    DEFAULT_HEADER_VALUE = headerValue;
  }

  public static String getDefaultHeaderValue() {
    return DEFAULT_HEADER_VALUE;
  }

  public static void setUnsupportedHeaderValue(String headerValue) {
    UNSUPPORTED_HEADER_VALUE = headerValue;
  }

  public static String getUnsupportedHeaderValue() {
    return UNSUPPORTED_HEADER_VALUE;
  }

  public static void setInvalidHeaderValue(String headerValue) {
    INVALID_HEADER_VALUE = headerValue;
  }

  public static String getInvalidHeaderValue() {
    return INVALID_HEADER_VALUE;
  }

  /**
   * Method to get test-ready RequestSpecBuilder based on the header and test type passed
   * 
   * @param builder
   * @param header
   * @param testType
   * @return
   */
  private static RequestSpecBuilder getTestReqSpecBuilder(RequestSpecBuilder builder,
      HeaderItem header, TestType testType) {
    String headerValue =
        (testType == TestType.INVALID) ? INVALID_HEADER_VALUE : UNSUPPORTED_HEADER_VALUE;

    builder.addHeader(header.toString(), headerValue);

    return builder;
  }

  /**
   * Get default valid RequestSpecBuilder
   * 
   * @return
   */
  private static RequestSpecBuilder getDefaultTestReqSpecBuilder() {
    RequestSpecBuilder builder = new RequestSpecBuilder();
    builder.addHeader(Headers.CONTENT_TYPE.toString(), DEFAULT_HEADER_VALUE);
    builder.addHeader(Headers.ACCEPT.toString(), DEFAULT_HEADER_VALUE);
    builder.addHeader(Headers.CLIENT_API_KEY.toString(), Headers.DEFAULT_API_KEY);

    return builder;
  }

  /**
   * Method to create a call and fire a call against an api which is configured correctly with the
   * exception of the header specified and the type of negative test.
   * 
   * @param header
   * @param testType
   * @param method
   * @param service
   * @param object
   * @return
   */
  public static Response negativeHeaderTest(HeaderItem header, TestType testType, Method method,
      String service, ModelObject object) {
    RequestSpecBuilder builder = getDefaultTestReqSpecBuilder();

    return negativeHeaderTest(builder, header, testType, method, service, object);
  }

  /**
   * Method to create a call and fire a call against an api which is configured correctly with the
   * exception of the header specified and the type of negative test.
   * 
   * @param builder
   * @param header
   * @param testType
   * @param method
   * @param service
   * @param object
   * @return
   */
  public static Response negativeHeaderTest(RequestSpecBuilder builder, HeaderItem header,
      TestType testType, Method method, String service, ModelObject object) {

    ResponseSpecification expResponse =
        (testType == TestType.INVALID) ? ExpectedResponse.NOT_ACCEPTABLE_RESPONSE
            : ExpectedResponse.UNSUPPORTED_MEDIA_TYPE_RESPONSE;

    return negativeHeaderTest(expResponse, builder, header, testType, method, service, object);
  }

  /**
   * Method to create a call and fire a call against an api which is configured correctly with the
   * exception of the header specified and the type of negative test.
   * 
   * @param expResponse
   * @param reqBuilder
   * @param header
   * @param testType
   * @param method
   * @param service
   * @param object
   * @return
   */
  public static Response negativeHeaderTest(ResponseSpecification expResponse,
      RequestSpecBuilder reqBuilder, HeaderItem header, TestType testType, Method method,
      String service, ModelObject object) {

    // Prep the request with the test spcifications
    RequestSpecBuilder builder = getTestReqSpecBuilder(reqBuilder, header, testType);
    // validate the request has all the necessary service-specific specs to complete the call.
    RequestSpecBuilder validatedBuilder = validateBuilder(builder, object, method);
    validatedBuilder.log(LogDetail.ALL);
    return RestApiController.callRestAPI(service, object.getObjectPath(), validatedBuilder,
        expResponse, method, true);
  }

  /**
   * Method to create a call and fire a call against an api which is configured correctly with the
   * exception excluding an Authorization header which normally contains a bearer token.
   * 
   * @param method
   * @param service
   * @param object
   * @return
   */
  public static Response negativeAuthTest(Method method, String service, ModelObject object) {
    return negativeAuthTest(null, method, service, object);
  }

  /**
   * Method to create a call and fire a call against an api which is configured correctly with the
   * exception excluding an Authorization header which normally contains a bearer token.
   * 
   * @param builder
   * @param method
   * @param service
   * @param object
   * @return
   */
  public static Response negativeAuthTest(RequestSpecBuilder builder, Method method, String service,
      ModelObject object) {
    if (builder == null) {
      builder = getDefaultTestReqSpecBuilder();
    }

    RequestSpecBuilder validatedBuilder = validateBuilder(builder, object, method);
    validatedBuilder.log(LogDetail.ALL);
    Response response = RestApiController.callRestAPI(service, object.getObjectPath(),
        validatedBuilder, ExpectedResponse.NOT_AUTH_RESPONSE, method, false);
    return response;
  }

  /**
   * 
   * Set of convenience methods to help with BoilerPlate testing
   * 
   */


  public static Response testGetWithInvalidAcceptHeader(String service, ModelObject object) {
    return negativeHeaderTest(Headers.ACCEPT, TestType.INVALID, Method.GET, service, object);
  }

  public static Response testGetWithUnsupportedAcceptHeader(String service, ModelObject object) {
    return negativeHeaderTest(Headers.ACCEPT, TestType.UNSUPPORTED, Method.GET, service, object);
  }

  public static Response testGetWithInvalidAPIKey(String service, ModelObject object) {
    return negativeHeaderTest(Headers.CLIENT_API_KEY, TestType.INVALID, Method.GET, service,
        object);
  }

  public static Response testGetUnAuthenticated(String service, ModelObject object) {
    return negativeAuthTest(Method.GET, service, object);
  }

  public static Response testPostWithInvalidAcceptHeader(String service, ModelObject object) {
    return negativeHeaderTest(Headers.ACCEPT, TestType.INVALID, Method.POST, service, object);
  }

  public static Response testPostWithUnsupportedAcceptHeader(String service, ModelObject object) {
    return negativeHeaderTest(Headers.ACCEPT, TestType.UNSUPPORTED, Method.POST, service, object);
  }

  public static Response testPostWithInvalidAPIKey(String service, ModelObject object) {
    return negativeHeaderTest(Headers.CLIENT_API_KEY, TestType.INVALID, Method.POST, service,
        object);
  }

  public static Response testPostUnAuthenticated(String service, ModelObject object) {
    return negativeAuthTest(Method.POST, service, object);
  }

  public static Response testPutWithInvalidAcceptHeader(String service, ModelObject object) {
    return negativeHeaderTest(Headers.ACCEPT, TestType.INVALID, Method.PUT, service, object);
  }

  public static Response testPutWithUnsupportedAcceptHeader(String service, ModelObject object) {
    return negativeHeaderTest(Headers.ACCEPT, TestType.UNSUPPORTED, Method.PUT, service, object);
  }

  public static Response testPutWithInvalidAPIKey(String service, ModelObject object) {
    return negativeHeaderTest(Headers.CLIENT_API_KEY, TestType.INVALID, Method.PUT, service,
        object);
  }

  public static Response testPutUnAuthenticated(String service, ModelObject object) {
    return negativeAuthTest(Method.PUT, service, object);
  }

  public static Response testDeleteWithInvalidAPIKey(String service, ModelObject object) {
    return negativeHeaderTest(Headers.CLIENT_API_KEY, TestType.INVALID, Method.DELETE, service,
        object);
  }

  public static Response testDeleteUnAuthenticated(String service, ModelObject object) {
    return negativeAuthTest(Method.DELETE, service, object);
  }
}
