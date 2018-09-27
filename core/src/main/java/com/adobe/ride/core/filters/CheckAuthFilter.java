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

package com.adobe.ride.core.filters;

import org.apache.commons.lang3.Validate;

import com.adobe.ride.config.management.GlobalConstants;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * @author tedcasey
 *
 */
public class CheckAuthFilter implements Filter {

  public CheckAuthFilter(String callingServiceName) {
    Validate.notNull(callingServiceName);
  }

  public Response filter(FilterableRequestSpecification requestSpec,
      FilterableResponseSpecification responseSpec, FilterContext ctx) {

    if (!requestSpec.getHeaders().hasHeaderWithName("Authorization")) {

      // Authorization not attached, so use auth workflow.
      String tmp =
          (System.getProperty(GlobalConstants.TARGET_TEST_ENVIRONMENT_KEY) == null) ? "localhost"
              : System.getProperty(GlobalConstants.TARGET_TEST_ENVIRONMENT_KEY);

      // Check if running in localhost
      if (!(tmp.equals("localhost"))) {
        // Add your domain-specific auth workflow code
        /*-
         * Example: 
         * MyAuthenticationLib authlib = AuthSingleton; 
         * String token = "Bearer " + authlib.getToken(); 
         * requestSpec.header(new Header("Authorization", token));
         */
      }
    }

    final Response response = ctx.next(requestSpec, responseSpec);

    return response;
  }
}
