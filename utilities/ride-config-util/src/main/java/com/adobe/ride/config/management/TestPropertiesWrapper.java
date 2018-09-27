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

import java.util.Map;

/**
 * 
 * @author hook
 *
 */
public class TestPropertiesWrapper {
  private String propertyFileName;
  private Map<String, Object> testProperties = null;

  public TestPropertiesWrapper(Map<String, Object> testProperties) {
    Validation.isNullOrEmptyMap(testProperties);

    this.testProperties = testProperties;
  }

  public String getPropertyFileName() {
    return propertyFileName;
  }

  public Map<String, Object> getTestProperties() {
    return testProperties;
  }

  public void setPropertyFileName(String propertyFileName) {
    this.propertyFileName = propertyFileName;
  }

  public Object getProperty(String key, Object defaultKey) {
    if (!testProperties.containsKey(key)) {
      return defaultKey;
    }

    Object property = testProperties.get(key);

    if (property == null
        || (property.getClass().equals(String.class) && property.toString().isEmpty())) {
      return defaultKey;
    }

    return testProperties.get(key);
  }

  public Object getProperty(String key) {
    Object property = null;

    if (!testProperties.containsKey(key)) {
      return property;
    }

    property = testProperties.get(key);
    return property;
  }
}
