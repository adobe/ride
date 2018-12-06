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

import static io.restassured.RestAssured.given;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.adobe.ride.config.aop.TargetServiceConfiguration;
import com.adobe.ride.config.management.TestProperties;
import com.adobe.ride.core.RideCore;
import com.adobe.ride.core.globals.Headers;
import com.adobe.ride.utilities.model.ModelObject;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ConnectionConfig;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

/**
 * @author jpettit, tedcasey
 *
 */
public class RestApiController extends RideCore {

  private final static JSONParser parser = new JSONParser();

  public static Map<String, String> getContentAcceptsHeaders() {
    HashMap<String, String> map = new HashMap<String, String>();
    map.put(Headers.CONTENT_TYPE.toString(), Headers.CONTENT_TYPE.defaultValue());
    map.put(Headers.ACCEPT.toString(), Headers.ACCEPT.defaultValue());
    return map;
  }

  /**
   * Use to ensure logging is always enabled for RA requests.
   *
   * @param useDefaultHeaders If true, enables default content-type, accept, and client headers.
   * @param filters The custom Rest-Assured filter to apply to the request.
   * @return RequestSpecBuilder
   */
  public static RequestSpecBuilder getRequestBuilder(boolean useDefaultHeaders, Filter... filters) {
    RestAssured.useRelaxedHTTPSValidation();
    ConnectionConfig connectionConfig = new ConnectionConfig();
    // connectionConfig.dontCloseIdleConnectionsAfterEachResponse();
    RestAssured.config = RestAssured.config().connectionConfig(connectionConfig);
    RequestSpecBuilder tmp = new RequestSpecBuilder();
    tmp.setUrlEncodingEnabled(false);
    RestAssuredConfig config = RestAssured.config().encoderConfig(
        (EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)));
    tmp.setConfig(config);

    // Filter may be null
    boolean customLogging = false;
    if (filters != null) {
      for (Filter f : filters) {
        tmp.addFilter(f);
        if (f instanceof RequestLoggingFilter) {
          customLogging = true;
        }
      }
    }

    if (!customLogging) {
      tmp.log(LogDetail.ALL);
    }

    if (useDefaultHeaders) {
      tmp.addHeaders(getContentAcceptsHeaders());
      tmp.addHeader(Headers.CLIENT_API_KEY.toString(), Headers.CLIENT_API_KEY.defaultValue());
    }

    return tmp;
  }

  /**
   * Utility method which returns a prepared RequestSpecBuilder to the API calls if the caller
   * hasn't passed one
   *
   * @param requestSpec Rest-assured definitions for the call.
   * @param object name of the target REST object, usually a segment in the path
   * @param method http action to be invoked (i.e. POST, GET, etc.)
   * @return RequestSpecBuilder
   */
  protected static RequestSpecBuilder validateBuilder(RequestSpecBuilder requestSpec,
      ModelObject object, Method method) {
    RequestSpecBuilder reqBuilder;
    if (requestSpec == null) {
      reqBuilder = getRequestBuilder(true);
    } else {
      reqBuilder = requestSpec;
    }
    if ((method == Method.POST || method == Method.PUT) && object.getObjectMetadata() != null) {
      JSONObject objData = object.getObjectMetadata();
      if (objData != null) {
        reqBuilder.setBody(objData);
      }
    }
    return reqBuilder;
  }

  /**
   * Static method for making Rest Assured DELETE requests.
   *
   * @param serviceName Name mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param expectedResponse RestAssured ResponseSpecification for validating the response
   * @param filters RestAssured Filters
   * @return Response
   */
  public static Response delete(String serviceName, String restAPI, RequestSpecBuilder reqBuilder,
      ResponseSpecification expectedResponse, Filter... filters) {
    return fireRestCall(serviceName, restAPI, reqBuilder, expectedResponse, Method.DELETE, filters);
  }

  /**
   * Static method for making Rest Assured GET requests.
   *
   * @param serviceName Name mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param expectedResponse RestAssured ResponseSpecification for validating the response
   * @param filters RestAssured Filters
   * @return Response
   */
  public static Response get(String serviceName, String restAPI, RequestSpecBuilder reqBuilder,
      ResponseSpecification expectedResponse, Filter... filters) {
    return fireRestCall(serviceName, restAPI, reqBuilder, expectedResponse, Method.GET, filters);
  }

  /**
   * Static method for making Rest Assured HEAD requests.
   *
   * @param serviceName Name mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param expectedResponse RestAssured ResponseSpecification for validating the response
   * @param filters RestAssured Filters
   * @return Response
   */
  public static Response head(String serviceName, String restAPI, RequestSpecBuilder reqBuilder,
      ResponseSpecification expectedResponse, Filter... filters) {
    return fireRestCall(serviceName, restAPI, reqBuilder, expectedResponse, Method.HEAD, filters);
  }

  /**
   * Static method for making Rest Assured OPTIONS requests.
   *
   * @param serviceName Name mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param expectedResponse RestAssured ResponseSpecification for validating the response
   * @param filters RestAssured Filters
   * @return Response
   */
  public static Response options(String serviceName, String restAPI, RequestSpecBuilder reqBuilder,
      ResponseSpecification expectedResponse, Filter... filters) {
    return fireRestCall(serviceName, restAPI, reqBuilder, expectedResponse, Method.OPTIONS, filters);
  }

  /**
   * Static method for making Rest Assured PATCH requests.
   *
   * @param serviceName Name mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param expectedResponse RestAssured ResponseSpecification for validating the response
   * @param filters RestAssured Filters
   * @return Response
   */
  public static Response patch(String serviceName, String restAPI, RequestSpecBuilder reqBuilder,
      ResponseSpecification expectedResponse, Filter... filters) {
    return fireRestCall(serviceName, restAPI, reqBuilder, expectedResponse, Method.PATCH, filters);
  }

  /**
   * Static method for making Rest Assured POST requests.
   *
   * @param serviceName Name mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param expectedResponse RestAssured ResponseSpecification for validating the response
   * @param filters RestAssured Filters
   * @return Response
   */
  public static Response post(String serviceName, String restAPI, RequestSpecBuilder reqBuilder,
      ResponseSpecification expectedResponse, Filter... filters) {
    return fireRestCall(serviceName, restAPI, reqBuilder, expectedResponse, Method.POST, filters);
  }

  /**
   * Static method for making Rest Assured PUT requests.
   *
   * @param serviceName Name mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param expectedResponse RestAssured ResponseSpecification for validating the response
   * @param filters RestAssured Filters
   * @return Response
   */
  public static Response put(String serviceName, String restAPI, RequestSpecBuilder reqBuilder,
      ResponseSpecification expectedResponse, Filter... filters) {
    return fireRestCall(serviceName, restAPI, reqBuilder, expectedResponse, Method.PUT, filters);
  }

  private static RequestSpecification prepRequest(String serviceName,
      RequestSpecBuilder reqBuilder, Filter... filters) {
    RequestSpecification req;
    TestProperties tp = TestProperties.getInstance();
    TargetServiceConfiguration targetService = tp.getTargetServiceConfig(serviceName);
    reqBuilder.setBaseUri(targetService.getURL());

    reqBuilder = nullCheckAndAddFilters(reqBuilder, filters);

    req = reqBuilder.build();

    return req;
  }

  /**
   * Static method for making Rest Assured REST requests.
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param expectedResponse RestAssured ResponseSpecification for validating the response
   * @param method http action to be invoked (i.e. POST, GET, etc.)
   * @param filters RestAssured Filters
   * @return Response
   */
  public static Response fireRestCall(String serviceName, String restAPI,
      RequestSpecBuilder reqBuilder, ResponseSpecification expectedResponse, Method method,
      Filter... filters) {

    RequestSpecification req = prepRequest(serviceName, reqBuilder, filters);

    switch (method) {
      case DELETE:
        return given(req).expect().spec(expectedResponse).log().all().when().delete(restAPI);
      case GET:
        return given(req).expect().spec(expectedResponse).log().all().when().get(restAPI);
      case HEAD:
        return given(req).expect().spec(expectedResponse).log().all().when().head(restAPI);
      case OPTIONS:
        return given(req).expect().spec(expectedResponse).log().all().when().options(restAPI);
      case PATCH:
        return given(req).expect().spec(expectedResponse).log().all().when().patch(restAPI);
      case POST:
        return given(req).expect().spec(expectedResponse).log().all().when().post(restAPI);
      case PUT:
        return given(req).expect().spec(expectedResponse).log().all().when().put(restAPI);
      case TRACE:
        return given(req).expect().spec(expectedResponse).log().all().when().request(method,
            restAPI);
      default:
        return null;
    }
  }

  /**
   * Static method for making Rest Assured REST requests with logging disabled.
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param expectedResponse RestAssured ResponseSpecification for validating the response
   * @param method http action to be invoked (i.e. POST, GET, etc.)
   * @param filters RestAssured Filters
   * @return Response
   */
  public static Response fireRestCallWithNoLogging(String serviceName, String restAPI,
      RequestSpecBuilder reqBuilder, ResponseSpecification expectedResponse, Method method,
      Filter... filters) {

    RequestSpecification req = prepRequest(serviceName, reqBuilder, filters);

    switch (method) {
      case DELETE:
        return given(req).expect().spec(expectedResponse).when().delete(restAPI);
      case GET:
        return given(req).expect().spec(expectedResponse).when().get(restAPI);
      case HEAD:
        return given(req).expect().spec(expectedResponse).when().head(restAPI);
      case OPTIONS:
        return given(req).expect().spec(expectedResponse).when().options(restAPI);
      case PATCH:
        return given(req).expect().spec(expectedResponse).when().patch(restAPI);
      case POST:
        return given(req).expect().spec(expectedResponse).when().post(restAPI);
      case PUT:
        return given(req).expect().spec(expectedResponse).when().put(restAPI);
      case TRACE:
        return given(req).expect().spec(expectedResponse).when().request(method, restAPI);
      default:
        return null;
    }
  }

  /**
   * Static method for making Rest Assured REST requests with logging disabled.
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param expectedResponse RestAssured ResponseSpecification for validating the response
   * @param method http action to be invoked (i.e. POST, GET, etc.)
   * @param filters RestAssured Filters
   * @return Response
   */
  public static InputStream openRestStream(String serviceName, String restAPI,
      RequestSpecBuilder reqBuilder, ResponseSpecification expectedResponse, Method method,
      Filter... filters) {
    RequestSpecification req;
    TestProperties tp = TestProperties.getInstance();
    TargetServiceConfiguration targetService = tp.getTargetServiceConfig(serviceName);
    reqBuilder.setBaseUri(targetService.getURL());

    reqBuilder = nullCheckAndAddFilters(reqBuilder, filters);

    req = reqBuilder.build();

    switch (method) {
      case DELETE:
        return given(req).expect().spec(expectedResponse).log().all().when().delete(restAPI)
            .asInputStream();
      case GET:
        return given(req).expect().spec(expectedResponse).log().all().when().get(restAPI)
            .asInputStream();
      case HEAD:
        return given(req).expect().spec(expectedResponse).log().all().when().head(restAPI)
            .asInputStream();
      case OPTIONS:
        return given(req).expect().spec(expectedResponse).log().all().when().options(restAPI)
            .asInputStream();
      case PATCH:
        return given(req).expect().spec(expectedResponse).log().all().when().patch(restAPI)
            .asInputStream();
      case POST:
        return given(req).expect().spec(expectedResponse).log().all().when().post(restAPI)
            .asInputStream();
      case PUT:
        return given(req).expect().spec(expectedResponse).log().all().when().put(restAPI)
            .asInputStream();
      case TRACE:
        return given(req).expect().spec(expectedResponse).log().all().when()
            .request(method, restAPI).asInputStream();
      default:
        return null;
    }
  }

  /**
   * Method to parse the response body into a JSONObject
   * 
   * @param body String representation of the response body
   * @return JSONObject
   */
  protected static JSONObject parseResponseBody(String body) {
    JSONObject returnObject = null;
    try {
      returnObject = (JSONObject) parser.parse(body);
    } catch (ParseException e) {
      logger.log(Level.SEVERE, e.getMessage());
    }

    return returnObject;
  }
}
