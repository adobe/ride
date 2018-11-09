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

import static com.adobe.ride.config.management.ServiceConstantsInterface.TARGET_TEST_ENVIRONMENT_KEY;
import static com.adobe.ride.config.management.ServiceConstantsInterface.TEST_COMMON_PROPERTY_FILE_NAME;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.Validate;
import org.testng.Assert;

import com.adobe.ride.config.aop.TargetServiceConfiguration;

/**
 * @author hook, jpettit, tedcasey
 */
public class TestProperties {

  public final static String CONFIG_FILES_DIRECTORY_NAME = "configs";
  private final static String CONFIG_FILES_DIR = "/" + CONFIG_FILES_DIRECTORY_NAME + "/";
  private final static String DECLARED_SERVICES_KEY = "declaredServices";
  private final static String IS_PRODUCTION_KEY = "isProduction";

  public static String[] declaredServices;
  private static Map<TestPropertyType, TestPropertiesWrapper> properties =
      new HashMap<TestPropertyType, TestPropertiesWrapper>();
  private static Map<String, TargetServiceConfiguration> services =
      new HashMap<String, TargetServiceConfiguration>();
  private static final TestProperties instanceHolder = new TestProperties();

  public TestProperties() {
    try {
      loadEnvironmentProperties();
      loadCommonProperties();
    } catch (ConfigurationException e) {
      Assert.fail("An error occurred trying to load all resources at: " + CONFIG_FILES_DIR);
    }

    System.out.println("All Configuration Management resources loaded successfully.");
  }

  public static String getPropertyAsString(TestPropertyType type, String key) {
    final String propertyValue = (String) TestProperties.getInstance().getProperty(type, key);
    return propertyValue;
  }

  public static String getPropertyAsString(TestPropertyType type, String key, String defaultValue) {
    final String propertyValue =
        (String) TestProperties.getInstance().getProperty(type, key, defaultValue);
    return propertyValue;
  }

  public static int getPropertyAsInt(TestPropertyType type, String key, int defaultValue) {
    Object obj = TestProperties.getInstance().getProperty(type, key, defaultValue);
    final String propertyValue = (obj instanceof String ? (String) obj : null);

    if (propertyValue != null) {
      return StringUtilities.safeParseInteger(propertyValue, defaultValue);
    } else {
      return (Integer) obj;
    }
  }

  public static String getServiceProperty(TestPropertyType type, String service, String property) {
    String key = service.concat(".").concat(property);
    @SuppressWarnings("unlikely-arg-type")
    boolean inThere = properties.containsKey(key);
    Validate.isTrue(inThere);
    final String propertyValue = (String) TestProperties.getInstance().getProperty(type, key);
    return propertyValue;
  }

  public static String getServiceProperty(TestPropertyType type, String service, String property,
      String defaultValue) {
    String key = service + "." + property;
    final String propertyValue =
        (String) TestProperties.getInstance().getProperty(type, key, defaultValue);
    return propertyValue;
  }

  public static int getPropertyAsInt(TestPropertyType type, String service, String property,
      int defaultValue) {
    String key = service + "." + property;
    Object obj = TestProperties.getInstance().getProperty(type, key, defaultValue);
    final String propertyValue = (obj instanceof String ? (String) obj : null);

    if (propertyValue != null) {
      return StringUtilities.safeParseInteger(propertyValue, defaultValue);
    } else {
      return (Integer) obj;
    }
  }

  public static TestProperties getInstance() {
    return instanceHolder;
  }

  private static Map<String, Object> loadPropertyFile(String path, TestPropertyType propertyType)
      throws ConfigurationException {
    PropertyFileConfiguration propertiesFile = new PropertyFileConfiguration(path);

    Map<String, Object> testProperties = propertiesFile.getAllProperties();

    if (testProperties.isEmpty()) {
      System.out.println("WARNING: No test properties in resource: " + path);
      return new HashMap<String, Object>();
    }

    TestPropertiesWrapper configProperties = new TestPropertiesWrapper(testProperties);
    properties.put(propertyType, configProperties);
    System.out.println("Test properties loaded from resource: " + path);

    return testProperties;
  }

  public TargetServiceConfiguration getTargetServiceConfig(String service) {
    TargetServiceConfiguration serviceConfigs = TestProperties.services.get(service);
    return serviceConfigs;
  }

  private Object getProperty(TestPropertyType testPropertyType, String key) {
    Validation.isNullOrEmptyParameter("testPropertyType", testPropertyType);
    Validation.isNullOrEmptyParameter("key", key);

    Object value = null;

    if (!properties.containsKey(testPropertyType)) {
      return value;
    }

    TestPropertiesWrapper testProperties = properties.get(testPropertyType);

    return testProperties.getProperty(key);
  }

  private Object getProperty(TestPropertyType testPropertyType, String key, Object defaultValue) {
    Validation.isNullOrEmptyParameter("testPropertyType", testPropertyType);
    Validation.isNullOrEmptyParameter("key", key);
    Validation.isNullOrEmptyParameter("defaultKey", defaultValue);

    if (!properties.containsKey(testPropertyType)) {
      return defaultValue;
    }

    TestPropertiesWrapper testProperties = properties.get(testPropertyType);

    return testProperties.getProperty(key, defaultValue);
  }

  private void loadEnvironmentProperties() throws ConfigurationException {
    // Loads in the target environment from System runtime property
    Map<String, Object> target = new HashMap<String, Object>();
    target.put(TARGET_TEST_ENVIRONMENT_KEY,
        System.getProperty(TARGET_TEST_ENVIRONMENT_KEY, "localhost"));
    TestPropertiesWrapper targetProperties = new TestPropertiesWrapper(target);
    properties.put(TestPropertyType.master, targetProperties);

    String propertiesPath =
        CONFIG_FILES_DIR + (String) target.get(TARGET_TEST_ENVIRONMENT_KEY) + ".properties";
    Map<String, Object> envProperties =
        loadPropertyFile(propertiesPath, TestPropertyType.environment);

    TestPropertiesWrapper environmentProperties = new TestPropertiesWrapper(envProperties);
    properties.put(TestPropertyType.environment, environmentProperties);

    createServicesEntries(envProperties);
  }

  private void createServicesEntries(Map<String, Object> envProperties) {
    // Set global constants
    declaredServices = envProperties.get(DECLARED_SERVICES_KEY).toString().split(",");
    GlobalConstants
        .setInProduction(Boolean.parseBoolean(envProperties.get(IS_PRODUCTION_KEY).toString()));

    // Configure Services
    for (int i = 0; i < declaredServices.length; i++) {
      String serviceName = declaredServices[i].replaceAll("[\\[\\]\\s]", "");
      if (!serviceName.isEmpty()) {
        String schema = envProperties
            .get(serviceName.concat(".").concat(TargetServiceConfiguration.SCHEMA_KEY)).toString();
        String endpoint = envProperties
            .get(serviceName.concat(".").concat(TargetServiceConfiguration.ENDPOINT_KEY))
            .toString();
        String port = envProperties
            .get(serviceName.concat(".").concat(TargetServiceConfiguration.PORT_KEY)).toString();
        String basePath = envProperties
            .get(serviceName.concat(".").concat(TargetServiceConfiguration.BASE_PATH_KEY))
            .toString();
        TargetServiceConfiguration svc =
            new TargetServiceConfiguration(serviceName, schema, endpoint, port, basePath);

        services.put(serviceName, svc);
      }
    }
  }

  private void loadCommonProperties() throws ConfigurationException {
    String commonFilePath = CONFIG_FILES_DIR + TEST_COMMON_PROPERTY_FILE_NAME;

    Map<String, Object> commonProps = loadPropertyFile(commonFilePath, TestPropertyType.common);
    TestPropertiesWrapper commonProperties = new TestPropertiesWrapper(commonProps);
    properties.put(TestPropertyType.common, commonProperties);
  }
}
