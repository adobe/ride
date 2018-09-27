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

package com.adobe.ride.config.management.test;

import static com.adobe.ride.config.management.ServiceConstantsInterface.TARGET_TEST_ENVIRONMENT_KEY;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Properties;

import org.testng.annotations.Test;

import com.adobe.ride.config.aop.TargetService;
import com.adobe.ride.config.aop.TargetServiceConfiguration;
import com.adobe.ride.config.management.TestProperties;

/**
 * @author jpettit, tedcasey
 *
 */
public class ConfigManagementTest {

  private Properties prop;

  public void loadResFile() throws IOException {
    prop = new Properties();
    InputStream input = null;

    String environment = System.getProperty(TARGET_TEST_ENVIRONMENT_KEY, "localhost");
    input =
        getClass().getClassLoader().getResourceAsStream("configs/" + environment + ".properties");
    prop.load(input);
  }

  @Test(suiteName = "smoke", groups = "integration", enabled = true)
  public void testAnnotationUsage() throws IOException {
    loadResFile();
    Method m;
    try {
      m = this.getClass().getMethod("testAnnotationUsage");

      for (Annotation a : m.getAnnotations()) {
        if (a instanceof TargetService) {
          TargetService t = (TargetService) a;
          String service_name = t.value();
          TestProperties tp = TestProperties.getInstance();
          TargetServiceConfiguration targetService = tp.getTargetServiceConfig(service_name);
          assertEquals(targetService.schema(), prop
              .getProperty(t.value().concat(".").concat(TargetServiceConfiguration.SCHEMA_KEY)));
          assertEquals(targetService.endpoint(), prop
              .getProperty(t.value().concat(".").concat(TargetServiceConfiguration.ENDPOINT_KEY)));
          assertEquals(targetService.port(), Integer.parseInt(
              prop.getProperty(t.value().concat(".").concat(TargetServiceConfiguration.PORT_KEY))));
          assertEquals(targetService.basePath(), prop
              .getProperty(t.value().concat(".").concat(TargetServiceConfiguration.BASE_PATH_KEY)));
        }
      }
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
  }
}
