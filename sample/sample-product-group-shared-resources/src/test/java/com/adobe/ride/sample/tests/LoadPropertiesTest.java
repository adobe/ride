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

package com.adobe.ride.sample.tests;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author tedcasey
 *
 */
public class LoadPropertiesTest {

  @Test
  public void testLoadProperties() throws IOException {
    Properties prop = new Properties();
    InputStream input = null;

    input = getClass().getClassLoader().getResourceAsStream("configs/localhost.properties");

    // load properties file
    prop.load(input);
    String[] declaredServices = prop.getProperty("declaredServices").split(",");

    for (int i = 0; i < declaredServices.length; i++) {
      testAndDumpConfig(prop, declaredServices[i]);
    }

  }

  private void testAndDumpConfig(Properties prop, String service) {
    String schema = prop.getProperty(service + ".schema");
    String port = prop.getProperty(service + ".port");
    String endpoint = prop.getProperty(service + ".endpoint");
    String basePath = prop.getProperty(service + ".basePath");
    // String user = prop.getProperty(service+".defaultUser");
    // String password = prop.getProperty(service+".defaultPassword");

    // get the property value and print it out
    Assert.assertEquals(schema, "http");
    Assert.assertEquals(port, "8080");
    Assert.assertNotNull(endpoint);
    Assert.assertNotNull(basePath);
    // Assert.assertNotNull(user);
    // Assert.assertEquals(password, "userPassword");

    System.out.println("");
    System.out.println(service + " Schema: " + schema);
    System.out.println(service + " Port: " + port);
    System.out.println(service + " Endpoint: " + endpoint);
    System.out.println(service + " Path: " + basePath);
    // System.out.println(service+" defaultUser: "+user);
    // System.out.println(service+" defaultPassword: "+password);
  }


}
