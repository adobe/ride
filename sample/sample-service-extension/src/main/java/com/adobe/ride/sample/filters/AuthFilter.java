/*************************************************************************
 *
 * ADOBE CONFIDENTIAL ___________________
 *
 * Copyright 2018 Adobe Systems Incorporated All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of Adobe Systems
 * Incorporated and its suppliers, if any. The intellectual and technical concepts contained herein
 * are proprietary to Adobe Systems Incorporated and its suppliers and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction of this material is strictly
 * forbidden unless prior written permission is obtained from Adobe Systems Incorporated.
 *
 **************************************************************************/

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
