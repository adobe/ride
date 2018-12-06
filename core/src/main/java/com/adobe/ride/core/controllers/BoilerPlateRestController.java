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
import io.restassured.filter.Filter;
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

  /**
   * Method to set the default value of to be used for a variety of headers
   * 
   * @param headerValue default value to be used
   */
  public static void setDefaultHeaderValue(String headerValue) {
    DEFAULT_HEADER_VALUE = headerValue;
  }

  /**
   * Get default value to be used for a variety of headers
   * 
   * @return String
   */
  public static String getDefaultHeaderValue() {
    return DEFAULT_HEADER_VALUE;
  }

  /**
   * Set the default value to be used when testing an unsupported header value
   * 
   * @param headerValue value to be used
   */
  public static void setUnsupportedHeaderValue(String headerValue) {
    UNSUPPORTED_HEADER_VALUE = headerValue;
  }

  /**
   * Get default value to be used when testing unsupported header values
   * 
   * @return String
   */
  public static String getUnsupportedHeaderValue() {
    return UNSUPPORTED_HEADER_VALUE;
  }

  /**
   * Set the default value to be used when testing an invalid header value.
   * 
   * @param headerValue value to be used.
   */
  public static void setInvalidHeaderValue(String headerValue) {
    INVALID_HEADER_VALUE = headerValue;
  }

  /**
   * Get default value to be used when testing invalid header values
   * 
   * @return String
   */
  public static String getInvalidHeaderValue() {
    return INVALID_HEADER_VALUE;
  }

  /**
   * Method to get test-ready RequestSpecBuilder based on the header and test type passed
   * 
   * @param builder Rest-Assured RequestSpecBuilder builder to be used in test call.
   * @param header name of the header to be tested
   * @param testType type of test to be run on the header
   * @return RequestSpecBuilder modified specification.
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
   * @return RequestSpecBuilder
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
   * @param header header to be tested
   * @param testType type of test to be run
   * @param method http action to be invoked (i.e. POST, PUT, GET)
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param object ModelObject used in conjunction with the call
   * @param filters Any Rest-Assured filters to be used in the call
   * @return Response
   */
  public static Response negativeHeaderTest(HeaderItem header, TestType testType, Method method,
      String serviceName, ModelObject object, Filter... filters) {
    RequestSpecBuilder builder = getDefaultTestReqSpecBuilder();

    return negativeHeaderTest(builder, header, testType, method, serviceName, object, filters);
  }

  /**
   * Method to create a call and fire a call against an api which is configured correctly with the
   * exception of the header specified and the type of negative test.
   * 
   * @param reqBuilder Rest-Assured RequestSpecBuilder builder upon which to base the test.
   * @param header header to be tested
   * @param testType type of test to be run
   * @param method http action to be invoked (i.e. POST, PUT, GET)
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param object ModelObject used in conjunction with the call
   * @param filters Rest-Assured filters to be used in the call
   * @return Response
   */
  public static Response negativeHeaderTest(RequestSpecBuilder reqBuilder, HeaderItem header,
      TestType testType, Method method, String serviceName, ModelObject object, Filter... filters) {

    ResponseSpecification expResponse =
        (testType == TestType.INVALID) ? ExpectedResponse.NOT_ACCEPTABLE_RESPONSE
            : ExpectedResponse.UNSUPPORTED_MEDIA_TYPE_RESPONSE;

    return negativeHeaderTest(expResponse, reqBuilder, header, testType, method, serviceName,
        object, filters);
  }

  /**
   * Method to create a call and fire a call against an api which is configured correctly with the
   * exception of the header specified and the type of negative test.
   * 
   * @param expResponse Rest-assured ResponseSpecification defining the expected response
   * @param reqBuilder Rest-Assured RequestSpecBuilder builder upon which to base the test
   * @param header header to be tested
   * @param testType type of test to be run
   * @param method http action to be invoked (i.e. POST, PUT, GET)
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param object ModelObject used in conjunction with the call
   * @param filters Rest-Assured filters to be used in the call
   * @return Response
   */
  public static Response negativeHeaderTest(ResponseSpecification expResponse,
      RequestSpecBuilder reqBuilder, HeaderItem header, TestType testType, Method method,
      String serviceName, ModelObject object, Filter... filters) {

    // Prep the request with the test spcifications
    RequestSpecBuilder builder = getTestReqSpecBuilder(reqBuilder, header, testType);
    // validate the request has all the necessary service-specific specs to complete the call.
    RequestSpecBuilder validatedBuilder = validateBuilder(builder, object, method);
    validatedBuilder.log(LogDetail.ALL);
    return RestApiController.fireRestCall(serviceName, object.getObjectPath(), validatedBuilder,
        expResponse, method, filters);
  }

  /**
   * Method to create a call and fire a call against an api which is configured correctly with the
   * exception excluding an Authorization header which normally contains a bearer token.
   * 
   * @param method http action to be invoked (i.e. POST, PUT, GET)
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param object ModelObject used in conjunction with the call
   * @return Response
   */
  public static Response negativeAuthTest(Method method, String serviceName, ModelObject object) {
    return negativeAuthTest(null, method, serviceName, object);
  }

  /**
   * Method to create a call and fire a call against an api which is configured correctly with the
   * exception excluding an Authorization header which normally contains a bearer token.
   * 
   * @param reqBuilder Rest-Assured RequestSpecBuilder builder upon which to base the test.
   * @param method http action to be invoked (i.e. POST, PUT, GET)
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param object ModelObject used in conjunction with the call
   * @return Response
   */
  public static Response negativeAuthTest(RequestSpecBuilder reqBuilder, Method method,
      String serviceName, ModelObject object) {
    if (reqBuilder == null) {
      reqBuilder = getDefaultTestReqSpecBuilder();
    }

    RequestSpecBuilder validatedBuilder = validateBuilder(reqBuilder, object, method);
    validatedBuilder.log(LogDetail.ALL);
    Response response = RestApiController.fireRestCall(serviceName, object.getObjectPath(),
        validatedBuilder, ExpectedResponse.NOT_AUTH_RESPONSE, method);
    return response;
  }

  /**
   * 
   * Set of convenience methods to help with BoilerPlate testing
   * 
   */


  /**
   * Boilerplate test for GET with invalid Accept header.
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param object ModelObject used in conjunction with the call
   * @param filters Any Rest-Assured filters to be used in the call
   * @return Response
   */
  public static Response testGetWithInvalidAcceptHeader(String serviceName, ModelObject object,
      Filter... filters) {
    return negativeHeaderTest(Headers.ACCEPT, TestType.INVALID, Method.GET, serviceName, object,
        filters);
  }

  /**
   * Boilerplate test for GET with unsupported Accept header.
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param object ModelObject used in conjunction with the call
   * @param filters Rest-Assured filters to be used in the call
   * @return Response
   */
  public static Response testGetWithUnsupportedAcceptHeader(String serviceName, ModelObject object,
      Filter... filters) {
    return negativeHeaderTest(Headers.ACCEPT, TestType.UNSUPPORTED, Method.GET, serviceName, object,
        filters);
  }

  /**
   * Boilerplate test for unauthenticated GET call.
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param object ModelObject used in conjunction with the call.
   * @return Response
   */
  public static Response testGetUnAuthenticated(String serviceName, ModelObject object) {
    return negativeAuthTest(Method.GET, serviceName, object);
  }

  /**
   * Boilerplate test for POST with invalid Accept header
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param object ModelObject used in conjunction with the call
   * @param filters Rest-Assured filters to be used in the call
   * @return Response
   */
  public static Response testPostWithInvalidAcceptHeader(String serviceName, ModelObject object,
      Filter... filters) {
    return negativeHeaderTest(Headers.ACCEPT, TestType.INVALID, Method.POST, serviceName, object,
        filters);
  }

  /**
   * Boilerplate test for POST with unsupported Accept header.
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param object ModelObject used in conjunction with the call
   * @param filters Rest-Assured filters to be used in the call
   * @return Response
   */
  public static Response testPostWithUnsupportedAcceptHeader(String serviceName, ModelObject object,
      Filter... filters) {
    return negativeHeaderTest(Headers.ACCEPT, TestType.UNSUPPORTED, Method.POST, serviceName,
        object, filters);
  }

  /**
   * Boilerplate test for unauthenticated POST call.
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param object ModelObject used in conjunction with the call
   * @return Response
   */
  public static Response testPostUnAuthenticated(String serviceName, ModelObject object) {
    return negativeAuthTest(Method.POST, serviceName, object);
  }

  /**
   * Boilerplate test for PUT with invalid Accept header.
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param object ModelObject used in conjunction with the call
   * @param filters Rest-Assured filters to be used in the call
   * @return Response
   */
  public static Response testPutWithInvalidAcceptHeader(String serviceName, ModelObject object,
      Filter... filters) {
    return negativeHeaderTest(Headers.ACCEPT, TestType.INVALID, Method.PUT, serviceName, object,
        filters);
  }

  /**
   * Boilerplate test for PUT with unsupported Accept header.
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param object ModelObject used in conjunction with the call
   * @param filters Rest-Assured filters to be used in the call
   * @return Response
   */
  public static Response testPutWithUnsupportedAcceptHeader(String serviceName, ModelObject object,
      Filter... filters) {
    return negativeHeaderTest(Headers.ACCEPT, TestType.UNSUPPORTED, Method.PUT, serviceName, object,
        filters);
  }

  /**
   * Boilerplate test for unauthenticated PUT call.
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param object ModelObject used in conjunction with the call
   * @return Response
   */
  public static Response testPutUnAuthenticated(String serviceName, ModelObject object) {
    return negativeAuthTest(Method.PUT, serviceName, object);
  }

  /**
   * Boilerplate test for unauthenticated DELETE call.
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param object ModelObject used in conjunction with the call
   * @return Response
   */
  public static Response testDeleteUnAuthenticated(String serviceName, ModelObject object) {
    return negativeAuthTest(Method.DELETE, serviceName, object);
  }
}
