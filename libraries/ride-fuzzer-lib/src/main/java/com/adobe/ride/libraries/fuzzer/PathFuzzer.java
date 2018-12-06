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

package com.adobe.ride.libraries.fuzzer;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import com.adobe.ride.libraries.fuzzer.engines.PathEngine;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;

/**
 * Class for fuzzing paths for arbitrary urls
 * 
 * Work in Progress
 * 
 * @author tedcasey
 * 
 */
@Test
public class PathFuzzer {
  protected String serviceName;
  protected String fuzzPath;
  protected Method RESTmethod;
  private RequestSpecBuilder requestSpecBldr;
  private String[] pathSet;
  private Object[][] fuzzSet;

  /**
   * Constructor for the Path Fuzzer.
   * 
   * @param serviceName String representation of the service name, maps back to a config file for
   *        environment configuration
   * @param reqSpec RequestSpecBuilder to be passed in the REST call sent to the core.
   * @param pathToBeFuzzed standard REST api path
   * @param method http action to be invoked (i.e. POST, GET, etc.)
   */
  public PathFuzzer(String serviceName, RequestSpecBuilder reqSpec, String pathToBeFuzzed,
      Method method) {
    this.serviceName = serviceName;
    requestSpecBldr = reqSpec;
    requestSpecBldr.setUrlEncodingEnabled(true);
    RESTmethod = method;
    // Remove leading slash, if passed
    fuzzPath =
        (pathToBeFuzzed.startsWith("/")) ? pathToBeFuzzed.substring(1, pathToBeFuzzed.length() - 1)
            : pathToBeFuzzed;
    // splt path for fuzzing path parts
    pathSet = fuzzPath.split("/");
    fuzzSet = populatePropertiesSet(pathSet);

    fuzzPath = pathToBeFuzzed;
  }

  /**
   * Method which turns the path into a dataprovider for the fuzzer to use.
   * 
   * @param pathSet Array with the split pieces of the path
   * @return
   */
  private Object[][] populatePropertiesSet(String[] pathSet) {
    Object[][] popTemp = new Object[pathSet.length][2];
    for (int i = 0; i < pathSet.length; i++) {
      popTemp[i][0] = fuzzPath;
      popTemp[i][1] = pathSet[i];
    }
    return popTemp;
  }

  /**
   * Method with drives the data to the engine.
   * 
   * @return Object[]
   */
  @Factory
  public Object[] fuzzPath() {
    Object[] result = new Object[fuzzSet.length];
    for (int i = 0; i < fuzzSet.length; i++) {
      String path = fuzzSet[i][0].toString();
      String target = fuzzSet[i][1].toString();
      result[i] = new PathEngine(serviceName, requestSpecBldr, path, target, RESTmethod);
    }
    return result;
  }

}
