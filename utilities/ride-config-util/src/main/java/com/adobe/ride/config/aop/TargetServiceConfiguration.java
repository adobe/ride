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
   * Constructor for the TargetServiceConfiguration class. All of these arguments will be passed in
   * from the properties files as part of the config-util workflow.
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in resources
   * @param schema internet protocol schema used in the endpoint (i.e. http, https, etc.)
   * @param endpoint endpoint of the service
   * @param port port the service uses (i.e. 80, 443, etc.)
   * @param basePath if warranted ("/" for no base path)
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

  /**
   * Returns the name of the service url being constructed.
   * 
   * @return String
   */
  public String serviceName() {
    return serviceName;
  }

  /**
   * Returns the schema of the service url being constructed.
   * 
   * @return String
   */
  public String schema() {
    return schema;
  }

  /**
   * Returns the endpoint of the service url being constructed.
   * 
   * @return String
   */
  public String endpoint() {
    return endpoint;
  }

  /**
   * Returns the port of the service url being constructed.
   * 
   * @return String
   */
  public int port() {
    return port;
  }

  /**
   * Returns the base path of the service url being constructed.
   * 
   * @return String
   */
  public String basePath() {
    return basePath;
  }

  /**
   * Returns the base uri of the service url being constructed.
   * 
   * @return String
   */
  public String getBaseURI() {
    return schema + "://" + endpoint;
  }

  /**
   * Returns the full url being constructed.
   * 
   * @return String
   */
  public String getURL() {
    return serviceURL;
  }
}
