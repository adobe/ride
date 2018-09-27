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

package com.adobe.ride.config.aop;

/**
 * Enumeration for easy access to target service configurations loaded from environment property
 * files.
 * 
 * @author jpettit, tedcasey
 */
public class TargetServiceConfiguration {

  public static final String SCHEMA_KEY = "schema";
  public static final String ENDPOINT_KEY = "endpoint";
  public static final String PORT_KEY = "port";
  public static final String BASE_PATH_KEY = "basePath";

  private final String serviceName;
  private final String schema;
  private final String endpoint;
  private final int port;
  private final String basePath;
  private final String serviceURL;

  /**
   * Constructor
   */
  public TargetServiceConfiguration(String serviceName, String schema, String endpoint, String port,
      String basePath) {
    this.serviceName = serviceName;
    this.schema = schema;
    this.endpoint = endpoint;
    this.port = Integer.parseInt(port);
    this.basePath = basePath;
    this.serviceURL = schema + "://" + endpoint + ":" + port + basePath;
  }

  public String serviceName() {
    return serviceName;
  }

  public String schema() {
    return schema;
  }

  public String endpoint() {
    return endpoint;
  }

  public int port() {
    return port;
  }

  public String basePath() {
    return basePath;
  }

  public String getBaseURI() {
    return schema + "://" + endpoint;
  }

  public String getURL() {
    return serviceURL;
  }
}
