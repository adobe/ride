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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
// import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.testng.Reporter;

import com.adobe.ride.config.aop.TargetService;
import com.cedarsoftware.util.io.JsonWriter;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * 
 * @author tedcasey, stlarson
 *
 */
public class TestNGReporterLogFilter implements Filter {

  private String splunkHostString;
  private String splunkIndexString;
  public String splunkEndPoint = "";
  public String requestID = "";

  /**
   * Report filter used by the framework core.
   * 
   * @param t
   * @throws UnsupportedEncodingException
   */
  public TestNGReporterLogFilter(TargetService t) throws UnsupportedEncodingException {
    Validate.notNull(t);
  }

  /**
   * Returns a string formatted for splunk for the current test based on request Id
   * 
   * @param reqId
   * @return
   */
  private String getSplunkSearchString(String splunkEndPoint, String reqId) {
    DateTime current = new DateTime();
    // convert to Unix Epoch time (which is what Splunk wants)
    String start = Long.toString(current.minusMinutes(2).getMillis() / 1000L);
    String stop = Long.toString(current.plusMinutes(2).getMillis() / 1000L);

    String requestIdString = (reqId != "") ? reqId : "";
    String searchUrl =
        splunkEndPoint + "search?q=search%20" + splunkIndexString + "%20" + splunkHostString + "%20"
            + requestIdString + "&" + "earliest=" + start + "&" + "latest=" + stop;

    return searchUrl;

  }

  /**
   * Parses the headers returned in the call and formats them for reporting.
   * 
   * @param headers
   * @return
   */
  private String getHeaderString(Headers headers) {
    String returnString = "";
    boolean first = true;
    for (Header h : headers) {
      if (first) {
        returnString += " " + h.getName() + ": " + h.getValue() + System.lineSeparator();
        first = false;
      } else {
        returnString += " " + h.getName() + ": " + h.getValue() + System.lineSeparator();
      }
    }
    return returnString;
  }

  /**
   * Formats a variety of request params for reporting.
   * 
   * @param reqParams
   * @return
   */
  private String getParamsString(Object reqParams) {
    @SuppressWarnings("unchecked")
    Map<String, String> params = (Map<String, String>) reqParams;
    String returnString = "";
    for (Map.Entry<String, String> entry : params.entrySet()) {
      returnString += " " + entry.getKey() + ": " + entry.getValue() + System.lineSeparator();
    }
    return returnString;
  }

  /**
   * Return string usable in the log
   * 
   * @param body
   * @return
   */
  private String getBodyString(Object body, String type) {
    String bodyString = "";

    if (body != null) {
      bodyString = body.toString().trim();
      if (!bodyString.isEmpty() && bodyString.length() != 0) {
        if (type.equals("request")) {
          try {
            bodyString = JsonWriter.formatJson(bodyString);
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else {
          bodyString = ((ResponseBody<?>) body).prettyPrint();
        }
      }
    }

    return bodyString;
  }

  /**
   * Main method for logging data
   */
  public Response filter(FilterableRequestSpecification requestSpec,
      FilterableResponseSpecification responseSpec, FilterContext ctx) {
    // Invoke the request by delegating to the next filter in the filter chain.
    final Response response = ctx.next(requestSpec, responseSpec);

    if (responseSpec.getStatusCode() != null) {
      if (responseSpec.getStatusCode().matches(response.statusCode())) {
        Reporter.log("Status code matched what was expected");
      } else {
        Reporter.log("REQUEST: " + System.lineSeparator() + "Request method: "
            + requestSpec.getMethod() + System.lineSeparator() + "Request Path: "
            + requestSpec.getURI() + System.lineSeparator() + "Request Params: "
            + getParamsString(requestSpec.getRequestParams()) + System.lineSeparator()
            + "Query Params: " + getParamsString(requestSpec.getQueryParams())
            + System.lineSeparator() + "Form Params: "
            + getParamsString(requestSpec.getFormParams()) + System.lineSeparator()
            + "Path Params: " + getParamsString(requestSpec.getPathParams())
            + System.lineSeparator() + "Headers: " + getHeaderString(requestSpec.getHeaders())
            + System.lineSeparator() + "Body: " + System.lineSeparator()
            + getBodyString(requestSpec.getBody(), "request") + System.lineSeparator()
            + System.lineSeparator() + System.lineSeparator() + "RESPONSE: "
            + System.lineSeparator() + " " + response.getStatusLine() + System.lineSeparator()
            + getHeaderString(response.getHeaders()) + System.lineSeparator()
            + getBodyString(response.getBody(), "response") + System.lineSeparator()
            + "SPLUNK SEARCH: " + getSplunkSearchString(splunkEndPoint, requestID)
            + System.lineSeparator() + System.lineSeparator() + System.lineSeparator()
            + System.lineSeparator());
      }
    } else {
      // Reporter.log("Status code was null");
    }
    return response;
  }
}
