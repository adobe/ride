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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 *
 * @author hook
 *
 */
public class PropertyFileConfiguration {

  private PropertiesConfiguration propertiesConfiguration = null;
  private String configurationPath = null;

  @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
      justification = "This was not authored by QE and may be refactored at a later time.")
  public PropertyFileConfiguration(String configurationPath) throws ConfigurationException {
    Validation.isNullOrEmptyParameter("configurationPath", configurationPath);

    this.configurationPath = configurationPath;

    InputStream in = getClass().getResourceAsStream(configurationPath);

    propertiesConfiguration = new PropertiesConfiguration();
    try {
      propertiesConfiguration.load(in);
    } catch (Exception e) {
      System.err.println("unable to load file: " + configurationPath);
      e.printStackTrace();
    }
  }

  public void reload() throws ConfigurationException {
    propertiesConfiguration.clear();
    propertiesConfiguration.reload();
  }

  public String getConfigurationPath() {
    return configurationPath;
  }

  @Override
  public String toString() {
    StringBuilder propertyValues = new StringBuilder();
    for (Iterator<?> it = propertiesConfiguration.getKeys(); it.hasNext();) {
      String key = (String) it.next();
      Object value = propertiesConfiguration.getProperty(key);
      propertyValues.append("key=" + key + " value=" + value.toString());
    }

    return propertyValues.toString();
  }

  @SuppressWarnings("unchecked")
  public <T> Map<String, T> getAllProperties() throws ConfigurationException {

    Map<String, T> map = new HashMap<String, T>();

    for (Iterator<?> it = propertiesConfiguration.getKeys(); it.hasNext();) {
      String key = (String) it.next();
      if (!map.containsKey(key)) {
        map.put(key, (T) propertiesConfiguration.getProperty(key));
      }
    }

    return map;
  }

  @SuppressWarnings("unchecked")
  public <T> T getProperty(String key) {
    Validation.isNullOrEmptyParameter("key", key);

    if (propertiesConfiguration != null) {
      T value = (T) propertiesConfiguration.getProperty(key);
      return value;
    }

    throw new MissingPropertyException("Cannot locate property with key=" + key);
  }
}
