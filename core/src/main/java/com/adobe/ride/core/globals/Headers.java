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

package com.adobe.ride.core.globals;

/**
 * 
 * @author tedcasey
 *
 */
public class Headers {

  public static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";
  public static final String JSON_CONTENT_NO_CHARSET_TYPE = "application/json";
  public static final String XML_CONTENT_TYPE = "application/xml";
  public static final String PROTOBUF_CONTENT_TYPE = "application/json;charset=UTF-8";
  public static final String DEFAULT_CONTENT_TYPE = JSON_CONTENT_TYPE;
  public static final String DEFAULT_API_KEY = "adobe-cloud-automation-default-key";

  public static HeaderItem CONTENT_TYPE = new HeaderItem("Content-Type", DEFAULT_CONTENT_TYPE);
  public static HeaderItem ACCEPT = new HeaderItem("Accept", DEFAULT_CONTENT_TYPE);
  public static HeaderItem CONTENT_LENGTH = new HeaderItem("Content-Length", "");
  public static HeaderItem CONTENT_MD5 = new HeaderItem("Content-MD5", "");
  public static HeaderItem ORIGIN = new HeaderItem("Origin", "");
  public static HeaderItem AUTHORIZATION = new HeaderItem("Authorization", "");

  public static HeaderItem CLIENT_API_KEY = new HeaderItem("x-api-key", DEFAULT_API_KEY);

  public static class HeaderItem {
    private String headerName;
    private String defaultValue;

    public HeaderItem(String headerName, String defaultValue) {
      this.headerName = headerName;
      this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
      return headerName;
    }

    public String defaultValue() {
      return defaultValue;
    }
  }
}
