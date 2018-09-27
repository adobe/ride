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

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

/**
 * 
 * @author tedcasey
 *
 */
public class ExpectedResponse {

  public static final ResponseSpecification OK_RESPONSE =
      ExpectedResponseTypes.OK.getResponseSpecification();
  public static final ResponseSpecification DELETED_RESPONSE =
      ExpectedResponseTypes.DELETED.getResponseSpecification();
  public static final ResponseSpecification CREATED_RESPONSE =
      ExpectedResponseTypes.CREATED.getResponseSpecification();
  public static final ResponseSpecification NOT_FOUND_RESPONSE =
      ExpectedResponseTypes.NOT_FOUND.getResponseSpecification();
  public static final ResponseSpecification NOT_AUTH_RESPONSE =
      ExpectedResponseTypes.UNAUTHORIZED.getResponseSpecification();
  public static final ResponseSpecification NOT_ALLOWED =
      ExpectedResponseTypes.METHOD_NOT_ALLOWED.getResponseSpecification();
  public static final ResponseSpecification NOT_ACCEPTABLE_RESPONSE =
      ExpectedResponseTypes.NOT_ACCEPTABLE.getResponseSpecification();
  public static final ResponseSpecification BAD_REQ_RESPONSE =
      ExpectedResponseTypes.BAD_REQUEST.getResponseSpecification();
  public static final ResponseSpecification CONFLICT_RESPONSE =
      ExpectedResponseTypes.CONFLICT.getResponseSpecification();
  public static final ResponseSpecification FORBIDDEN_RESPONSE =
      ExpectedResponseTypes.FORBIDDEN.getResponseSpecification();
  public static final ResponseSpecification ACCEPTED_RESPONSE =
      ExpectedResponseTypes.ACCEPTED.getResponseSpecification();
  public static final ResponseSpecification UNSUPPORTED_MEDIA_TYPE_RESPONSE =
      ExpectedResponseTypes.UNSUPPORTED_MEDIA_TYPE.getResponseSpecification();
  public static final ResponseSpecification UNPROCESSABLE_ENTITY_RESPONSE =
      ExpectedResponseTypes.UNPROCESSABLE_ENTITY.getResponseSpecification();
  public static final ResponseSpecification NO_RESPONSE = new ResponseSpecBuilder().build();
}
