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

package com.adobe.ride.config.management;

/**
 * Non instantiable utility class for storing global service key constants.
 *
 * @author jpettit, tedcasey
 */
public class GlobalConstants {

  
  // general property file driven constants
  public static final String TARGET_TEST_ENVIRONMENT_KEY = "target";
  // common property file driven constants
  public static final String TEST_COMMON_PROPERTY_FILE_NAME = "common.properties";
  private static boolean inProduction = false;

  /**
   * Private constructor - prevent instantiation
   */
  private GlobalConstants() {}

  public static boolean inProduction() {
    return inProduction;
  }

  public static void setInProduction(boolean inProduction) {
    GlobalConstants.inProduction = inProduction;
  }
}
