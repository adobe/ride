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

/**
 * Enum of log levels in the framework
 * 
 * @author tedcasey
 *
 */
public enum LogLevel {
  ALL("all"), ON_FAILURE("onFailure"), CUSTOM("custom"), NONE("none");

  private final String value;

  private LogLevel(String value) {
    this.value = value;
  }

  public String toString() {
    return value;
  }

  public static LogLevel parse(String type) {
    for (LogLevel t : LogLevel.values()) {
      if (t.value.equalsIgnoreCase(type)) {
        return t;
      }
    }
    return null;
  }
}
