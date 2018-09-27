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

package com.adobe.ride.sample.core;

import org.apache.http.HttpHeaders;

import com.adobe.ride.core.controllers.RestApiController;
import com.adobe.ride.core.types.MimeTypes;
import com.adobe.ride.sample.types.Service;
import com.adobe.ride.utilities.model.ModelObject;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;

/**
 * 
 * @author tedcasey
 *
 */
public class SampleServiceController extends RestApiController {

  protected static RequestSpecBuilder getDefaultReqSpecBuilder(Filter... filters) {
    RequestSpecBuilder builder;
    builder = RestApiController.getRequestBuilder(true, filters);
    builder.addHeader(HttpHeaders.ACCEPT, MimeTypes.APP_JSON.toString());
    builder.addHeader(HttpHeaders.CONTENT_TYPE, MimeTypes.APP_JSON.toString());
    return builder;
  }

  public static Response createOrUpdate(String objectPath, ModelObject object,
      ResponseSpecification expectedResponse) {
    RequestSpecBuilder reqBuilder = getDefaultReqSpecBuilder();
    reqBuilder.setBody(object.getObjectMetadata());
    return callRestAPI(Service.SAMPLE_SERVICE.toString(), objectPath, reqBuilder, expectedResponse,
        Method.PUT);
  }

  public static Response createOrUpdateObject(String objectPath, ModelObject object,
      ResponseSpecification expectedResponse, boolean addAuthorization) {
    RequestSpecBuilder reqBuilder = getDefaultReqSpecBuilder();
    reqBuilder.setBody(object.getObjectMetadata());
    return RestApiController.callRestAPI(Service.SAMPLE_SERVICE.toString(), objectPath, reqBuilder,
        expectedResponse, Method.PUT, addAuthorization);
  }

  public static Response createOrUpdateArrayObject(String objectPath, ModelObject object,
      ResponseSpecification expectedResponse, boolean addAuthorization) {
    RequestSpecBuilder reqBuilder = getDefaultReqSpecBuilder();
    reqBuilder.setBody(object.getObjectItems());
    return RestApiController.callRestAPI(Service.SAMPLE_SERVICE.toString(), objectPath, reqBuilder,
        expectedResponse, Method.PUT, addAuthorization);
  }

  public static Response get(String objectPath, ModelObject object,
      ResponseSpecification expectedResponse) {
    RequestSpecBuilder reqBuilder = getDefaultReqSpecBuilder();
    reqBuilder.setBody(object.getObjectMetadata());
    return RestApiController.callRestAPI(Service.SAMPLE_SERVICE.toString(), objectPath, reqBuilder,
        expectedResponse, Method.GET);
  }

  public static Response get(String objectPath, ModelObject object,
      ResponseSpecification expectedResponse, boolean addAuthorization) {
    RequestSpecBuilder reqBuilder = getDefaultReqSpecBuilder();
    reqBuilder.setBody(object.getObjectMetadata());
    return RestApiController.callRestAPI(Service.SAMPLE_SERVICE.toString(), objectPath, reqBuilder,
        expectedResponse, Method.GET, addAuthorization);
  }

  public static Response delete(String objectPath, ModelObject object,
      ResponseSpecification expectedResponse) {
    RequestSpecBuilder reqBuilder = getDefaultReqSpecBuilder();
    reqBuilder.setBody(object.getObjectMetadata());
    return RestApiController.callRestAPI(Service.SAMPLE_SERVICE.toString(), objectPath, reqBuilder,
        expectedResponse, Method.DELETE);
  }

  public static Response delete(String objectPath, ModelObject object,
      ResponseSpecification expectedResponse, boolean addAuthorization) {
    RequestSpecBuilder reqBuilder = getDefaultReqSpecBuilder();
    reqBuilder.setBody(object.getObjectMetadata());
    return RestApiController.callRestAPI(Service.SAMPLE_SERVICE.toString(), objectPath, reqBuilder,
        expectedResponse, Method.DELETE, addAuthorization);
  }
}
