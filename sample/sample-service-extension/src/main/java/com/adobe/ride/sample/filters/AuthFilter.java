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

package com.adobe.ride.sample.filters;

import org.apache.commons.lang3.Validate;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * @author tedcasey
 *
 */
public class AuthFilter implements Filter {
  private final String callingServiceName;

  public AuthFilter(String callingServiceName) {
    Validate.notNull(callingServiceName);
    this.callingServiceName = callingServiceName;
  }

  public AuthFilter() {
    this.callingServiceName = "";
  }

  public Response filter(FilterableRequestSpecification requestSpec,
      FilterableResponseSpecification responseSpec, FilterContext ctx) {

    if (!requestSpec.getHeaders().hasHeaderWithName("Authorization")) {

      // Token retrieved from some code invoked here
      String token = "somesupersecrettoken";
      requestSpec.header(new Header("Authorization", token));
    }

    final Response response = ctx.next(requestSpec, responseSpec);

    return response;
  }
}
