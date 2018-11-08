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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
  public static final TestProperties tp = TestProperties.getInstance();

  private static Filter authCheckFilter;
  // private static CoreGlobals globals = CoreGlobals.INSTANCE;

  public static void setAuthFilter(Filter authFilter) {
    authCheckFilter = authFilter;
  }

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
   * @param filter The custom Rest-Assured filter to apply to the request.
   * @return A static reference to a new {@link RequestSpecBuilder} instance.
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
   * @param requestSpec
   * @param object
   * @param method
   * @return
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
   * Use to ensure logging is always enabled for RA requests.
   *
   * @param filter The custom filter to apply to the request.
   * @return A static reference to a new {@link RequestSpecBuilder} instance.
   */
  public static <H extends Enum<H>> RequestSpecBuilder getRequestBuilder(Class<H> headers,
      Filter... filters) {
    RequestSpecBuilder tmp = new RequestSpecBuilder();

    // Filter may be null
    if (filters != null) {
      for (Filter f : filters) {
        tmp.addFilter(f);
      }
    }

    /*-
    for (H header : headers.getEnumConstants()) {
      if (header.isDefault()) {
        tmp.addHeader(header.toString(), header.defaultValue());
      }
    }
    */

    return tmp;
  }

  /**
   * Static method for making Rest Assured DELETE requests.
   *
   * @param serviceName Name mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param resBuilder RestAssured ResponseSpecBuilder for constructing RA response.
   * @param filter RestAssured Filter
   * @return RA Response object.
   */
  public static Response delete(String serviceName, String restAPI, RequestSpecBuilder reqBuilder,
      ResponseSpecification expectedResponse, Filter filter) {
    return fireRestCall(serviceName, restAPI, reqBuilder, expectedResponse, Method.DELETE, filter);
  }

  /**
   * Static method for making Rest Assured GET requests.
   *
   * @param serviceName Name mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param resBuilder RestAssured ResponseSpecBuilder for constructing RA response.
   * @param filter RestAssured Filter
   * @return RA Response object.
   */
  public static Response get(String serviceName, String restAPI, RequestSpecBuilder reqBuilder,
      ResponseSpecification expectedResponse, Filter filter) {
    return fireRestCall(serviceName, restAPI, reqBuilder, expectedResponse, Method.GET, filter);
  }

  /**
   * Static method for making Rest Assured HEAD requests.
   *
   * @param serviceName Name mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param resBuilder RestAssured ResponseSpecBuilder for constructing RA response.
   * @param filter RestAssured Filter
   * @return RA Response object.
   */
  public static Response head(String serviceName, String restAPI, RequestSpecBuilder reqBuilder,
      ResponseSpecification expectedResponse, Filter filter) {
    return fireRestCall(serviceName, restAPI, reqBuilder, expectedResponse, Method.HEAD, filter);
  }

  /**
   * Static method for making Rest Assured OPTIONS requests.
   *
   * @param serviceName Name mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param resBuilder RestAssured ResponseSpecBuilder for constructing RA response.
   * @param filter RestAssured Filter
   * @return RA Response object.
   */
  public static Response options(String serviceName, String restAPI, RequestSpecBuilder reqBuilder,
      ResponseSpecification expectedResponse, Filter filter) {
    return fireRestCall(serviceName, restAPI, reqBuilder, expectedResponse, Method.OPTIONS, filter);
  }

  /**
   * Static method for making Rest Assured PATCH requests.
   *
   * @param serviceName Name mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param resBuilder RestAssured ResponseSpecBuilder for constructing RA response.
   * @param filter RestAssured Filter
   * @return RA Response object.
   */
  public static Response patch(String serviceName, String restAPI, RequestSpecBuilder reqBuilder,
      ResponseSpecification expectedResponse, Filter filter) {
    return fireRestCall(serviceName, restAPI, reqBuilder, expectedResponse, Method.PATCH, filter);
  }

  /**
   * Static method for making Rest Assured POST requests.
   *
   * @param serviceName Name mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param resBuilder RestAssured ResponseSpecBuilder for constructing RA response.
   * @param filter RestAssured Filter
   * @return RA Response object.
   */
  public static Response post(String serviceName, String restAPI, RequestSpecBuilder reqBuilder,
      ResponseSpecification expectedResponse, Filter filter) {
    return fireRestCall(serviceName, restAPI, reqBuilder, expectedResponse, Method.POST, filter);
  }

  /**
   * Static method for making Rest Assured PUT requests.
   *
   * @param serviceName Name mapping to the config folder in resources.
   * @param restAPI The specific API to test ex: "/myApi".
   * @param reqBuilder RestAssured RequestSpecBuilder for constructing RA request.
   * @param resBuilder RestAssured ResponseSpecBuilder for constructing RA response.
   * @param filter RestAssured Filter
   * @return RA Response object.
   */
  public static Response put(String serviceName, String restAPI, RequestSpecBuilder reqBuilder,
      ResponseSpecification expectedResponse, Filter filter) {
    return fireRestCall(serviceName, restAPI, reqBuilder, expectedResponse, Method.PUT, filter);
  }

  public static Response fireRestCall(String callingService, String restAPI,
      RequestSpecBuilder reqBuilder, ResponseSpecification expectedResponse, Method method,
      Filter... filters) {
    RequestSpecification req;
    
    TestProperties tp = TestProperties.getInstance();
    TargetServiceConfiguration targetService = tp.getTargetServiceConfig(callingService);
    reqBuilder.setBaseUri(targetService.getURL());

    List<Filter> filterList = new ArrayList<Filter>();
    for (Filter f : filters) {
      if (f != null) {
        filterList.add(f);
      }
    }

    if (filterList.size() > 0) {
      reqBuilder.addFilters(filterList);
    }
    req = reqBuilder.build();

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

  public static Response fireRestCallWithNoLogging(String callingService, String restAPI,
      RequestSpecBuilder reqBuilder, ResponseSpecification expectedResponse, Method method,
      Filter... filters) {
    RequestSpecification req;
    TestProperties tp = TestProperties.getInstance();
    TargetServiceConfiguration targetService = tp.getTargetServiceConfig(callingService);
    reqBuilder.setBaseUri(targetService.getURL());

    List<Filter> filterList = new ArrayList<Filter>();
    for (Filter f : filters) {
      if (f != null) {
        filterList.add(f);
      }
    }

    if (filterList.size() > 0) {
      reqBuilder.addFilters(filterList);
    }

    req = reqBuilder.build();

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
   * Open a REST API call to a streaming service
   * 
   * @param callingService
   * @param restAPI
   * @param reqBuilder
   * @param expectedResponse
   * @param method
   * @param addAuthorization
   * @return
   */
  public static InputStream openRestStream(String callingService, String restAPI,
      RequestSpecBuilder reqBuilder, ResponseSpecification expectedResponse, Method method,
      Filter... filters) {
    RequestSpecification req;

    List<Filter> filterList = new ArrayList<Filter>();
    for (Filter f : filters) {
      if (f != null) {
        filterList.add(f);
      }
    }

    if (filterList.size() > 0) {
      reqBuilder.addFilters(filterList);
    }

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
   * Method to parse the response body of a call into an object that is common used for analysis
   * 
   * @param body String representation of the response body
   * @return
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
