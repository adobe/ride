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

import com.adobe.ride.core.types.LogLevel;

import io.restassured.config.LogConfig;

/**
 * Class to store global static constants available at runtime for all test which leverage the
 * framework.
 * 
 * @author tedcasey
 *
 */
public class CoreGlobals {

  public static CoreGlobals INSTANCE = new CoreGlobals();
  private final static String COMMAND_LINE_PROPERTY_APIKEY = "apiKey";
  private final static String COMMAND_LINE_PROPERTY_LOG_LEVEL = "logLevel";
  private final static String COMMAND_LINE_PROPERTY_TARGET = "target";
  private String target;
  private String APIKey;
  private LogLevel logLevel;
  private LogConfig customLogConfig;

  private CoreGlobals() {
    this.target = System.getProperty(COMMAND_LINE_PROPERTY_TARGET);

    // set apiKey to default if not specified
    if (System.getProperty(COMMAND_LINE_PROPERTY_APIKEY) != null) {
      APIKey = System.getProperty(COMMAND_LINE_PROPERTY_APIKEY);
    } else {
      APIKey = Headers.CLIENT_API_KEY.defaultValue();
    }

    // set Rest-assured loging to "all" if not specified
    if (System.getProperty(COMMAND_LINE_PROPERTY_LOG_LEVEL) != null) {
      if (null != LogLevel.valueOf(System.getProperty(COMMAND_LINE_PROPERTY_LOG_LEVEL))) {
        setLogLevel(LogLevel.valueOf(System.getProperty(COMMAND_LINE_PROPERTY_LOG_LEVEL)));
      } else {
        setLogLevel(LogLevel.ALL);
      }
    } else {
      setLogLevel(LogLevel.ALL);
    }
  }

  public String getAPIKey() {
    return APIKey;
  }

  public void setAPIKey(String key) {
    APIKey = key;
  }

  public LogLevel getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(LogLevel logLevel) {
    this.logLevel = logLevel;
  }

  public LogConfig getCustomLogConfig() {
    return customLogConfig;
  }

  public void setCustomLogConfig(LogConfig customLogConfig) {
    this.customLogConfig = customLogConfig;
  }

  public String getTarget() {
    return target;
  }
}
