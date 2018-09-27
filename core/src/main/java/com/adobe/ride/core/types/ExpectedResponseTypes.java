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

package com.adobe.ride.core.types;

import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_GATEWAY_TIMEOUT;
import static org.apache.http.HttpStatus.SC_LOCKED;
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED;
import static org.apache.http.HttpStatus.SC_NOT_ACCEPTABLE;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_REQUEST_TOO_LONG;
import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import static org.apache.http.HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

/**
 * @author jpettit
 *
 */
public enum ExpectedResponseTypes {

  // Any response code
  ANY(-1),

  // 2xx Response Codes
  /** 200 **/
  OK(SC_OK),
  /** 201 **/
  CREATED(SC_CREATED),
  /** 202 **/
  ACCEPTED(SC_ACCEPTED),
  /** 204 **/
  DELETED(SC_NO_CONTENT),

  // 4xx Response Codes
  /** 400 **/
  BAD_REQUEST(SC_BAD_REQUEST),
  /** 401 **/
  UNAUTHORIZED(SC_UNAUTHORIZED),
  /** 403 **/
  FORBIDDEN(SC_FORBIDDEN),
  /** 404 **/
  NOT_FOUND(SC_NOT_FOUND),
  /** 405 **/
  METHOD_NOT_ALLOWED(SC_METHOD_NOT_ALLOWED),
  /** 406 **/
  NOT_ACCEPTABLE(SC_NOT_ACCEPTABLE),
  /** 409 **/
  CONFLICT(SC_CONFLICT),
  /** 413 **/
  REQUEST_TOO_LONG(SC_REQUEST_TOO_LONG),
  /** 415 **/
  UNSUPPORTED_MEDIA_TYPE(SC_UNSUPPORTED_MEDIA_TYPE),
  /** 422 **/
  UNPROCESSABLE_ENTITY(SC_UNPROCESSABLE_ENTITY),
  /** 423 **/
  LOCKED(SC_LOCKED),

  // 5xx Response Codes
  /** 503 **/
  SERVICE_UNAVAILABLE(SC_SERVICE_UNAVAILABLE),
  /** 504 **/
  GATEWAY_TIMEOUT(SC_GATEWAY_TIMEOUT);

  private ExpectedResponseTypes(int status) {
    ResponseSpecBuilder bldr = new ResponseSpecBuilder();
    if (status != -1) {
      bldr.expectStatusCode(status);
    }
    this.response = bldr.build();
  }

  public ResponseSpecification getResponseSpecification() {
    return this.response;
  }

  private ResponseSpecification response;
}
