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

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Factory;

import com.adobe.ride.libraries.fuzzer.engines.MetadataEngine;
import com.adobe.ride.utilities.model.ModelObject;
import com.adobe.ride.utilities.model.exceptions.UnexpectedModelDefinitionException;
import com.adobe.ride.utilities.model.exceptions.UnexpectedModelPropertyTypeException;
import com.adobe.ride.utilities.model.types.ModelPropertyType;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;

/**
 * Class for REST API json body node fuzzing
 * 
 * @author tedcasey
 * 
 */
public class MetadataFuzzer {
  protected ModelObject entity;
  protected JSONObject properties;
  protected JSONObject instance;
  protected String targetService;
  protected Method requestMethod;
  protected String contentType;
  protected RequestSpecBuilder requestBuilder;

  /** Arrays to be dynamically populated with metadata info for fuzzing **/
  private Object[][] propertiesFuzzSet;

  // private Object[][] linksFuzzSet;
  // private Object[][] totalFuzzSet;

  /**
   * Method to initialize the Fuzzer factory and related engine subcomponents
   * 
   * @param serviceName String representation of the service name, maps back to a config file for
   *        environment configuration
   * @param entityToBeFuzzed ModelObject of a subclass thereof
   */
  public MetadataFuzzer(String targetService, ModelObject entityToBeFuzzed) {
    initializeFuzzer(targetService, entityToBeFuzzed, null, null, null);
  }

  /**
   * Method to initialize the Fuzzer factory and related engine subcomponents
   * 
   * @param serviceName String representation of the service name, maps back to a config file for
   *        environment configuration
   * @param entityToBeFuzzed ModelObject of a subclass thereof
   * @param builder RequestSpecBuilder custom specification to be used in the fuzzer calls. If null,
   *        a base spec with only Authorization, Content-Type, and Accepts set. The latter two set
   *        to a json variant,
   */
  public MetadataFuzzer(String targetService, ModelObject entityToBeFuzzed,
      RequestSpecBuilder builder) {
    initializeFuzzer(targetService, entityToBeFuzzed, builder, null, null);
  }

  /**
   * Method to initialize the Fuzzer factory and related engine subcomponents
   * 
   * @param serviceName String representation of the service name, maps back to a config file for
   *        environment configuration
   * @param entityToBeFuzzed ModelObject of a subclass thereof
   * @param builder RequestSpecBuilder custom specification to be used in the fuzzer calls. If null,
   *        a base spec with only Authorization, Content-Type, and Accepts set. The latter two set
   *        to a json variant,
   * @param method Method http verb to be used in the call. If null, PUT is used.
   */
  public MetadataFuzzer(String targetService, ModelObject entityToBeFuzzed,
      RequestSpecBuilder builder, Method method) {
    initializeFuzzer(targetService, entityToBeFuzzed, builder, method, null);
  }

  /**
   * Method to initialize the Fuzzer factory and related engine subcomponents
   * 
   * @param serviceName String representation of the service name, maps back to a config file for
   *        environment configuration
   * @param entityToBeFuzzed ModelObject of a subclass thereof
   * @param builder RequestSpecBuilder custom specification to be used in the fuzzer calls. If null,
   *        a base spec with only Authorization, Content-Type, and Accepts set. The latter two set
   *        to a json variant,
   * @param method Method http verb to be used in the call. If null, PUT is used.
   * @param contentType String content type header value (if null, default is
   *        "application/json;charset=utf-8")
   */
  public MetadataFuzzer(String targetService, ModelObject entityToBeFuzzed,
      RequestSpecBuilder builder, Method method, String contentType) {
    initializeFuzzer(targetService, entityToBeFuzzed, builder, method, contentType);
  }

  /**
   * Cruft method left over before streamlining. Leaving in for backwards compatibility
   * 
   * @param entityToBeFuzzed EntityObject subclass instance to be fuzzed.
   * @param requestMethod method with which to call the api (if null, default is PUT)
   * @param contentType string content type header value (if null, default is
   *        "application/json;charset=utf-8")
   */
  public MetadataFuzzer(String targetService, ModelObject entityToBeFuzzed, Method method,
      String contentType) {
    initializeFuzzer(targetService, entityToBeFuzzed, null, method, contentType);
  }

  private void initializeFuzzer(String targetService, ModelObject entityToBeFuzzed,
      RequestSpecBuilder requestBuilder, Method method, String contentType) {

    // Populate global properties
    this.entity = entityToBeFuzzed;
    this.requestMethod = method;
    this.targetService = targetService;
    this.properties = this.entity.getModelProperties();
    this.instance = this.entity.getObjectMetadata();
    this.requestMethod = method;
    this.contentType = contentType;
    this.requestBuilder = requestBuilder;
    propertiesFuzzSet = new Object[properties.keySet().size()][3];

    // Prep TestNG dataprovider to make reporting better.
    populateFuzzSet(properties, propertiesFuzzSet, "rootProperty");

    // Join root properties set with mined _links set
    // totalFuzzSet = joinArrays(propertiesFuzzSet, linksFuzzSet);
  }



  /**
   * Function which turns the properties of the model into a dataprovider for the fuzzer to use.
   * 
   * @param set JSONObject representing a set of metadata nodes.
   * @param fuzzSet The array in which the data will be pushed
   * @param propertyType Indicator specifying links or root property.
   * @throws UnexpectedModelPropertyTypeException
   */
  private void populateFuzzSet(JSONObject set, Object[][] fuzzSet, String propertyType) {
    try {
      Object[] keys = set.keySet().toArray();

      for (int i = 0; i < keys.length; i++) {
        JSONObject obj = (JSONObject) set.get(keys[i]);
        ModelPropertyType type = ModelObject.getModelPropertyType(obj);
        /*-
        if (keys[i].equals("_links")) {
          JSONObject linksProperties = (JSONObject) obj.get("properties");
          linksFuzzSet = new Object[linksProperties.keySet().size()][3];
          populateFuzzSet(linksProperties, linksFuzzSet, "linksProperty");
        }
        */

        fuzzSet[i][0] = keys[i].toString();
        fuzzSet[i][1] = (ModelPropertyType) type;
        fuzzSet[i][2] = propertyType;
      }
    } catch (UnexpectedModelPropertyTypeException e) {
      System.out.println("FUZZER ERROR: UnexpectedModelPropertyType:" + e.getMessage());
      e.printStackTrace();
    }

  }

  /**
   * Utility method to join two arrays.
   * 
   * @param arrays to be joined
   * @return
   */
  @SuppressWarnings("unused")
  private Object[][] joinArrays(Object[][]... arrays) {
    int length = 0;
    for (Object[] array : arrays) {
      length += array.length;
    }
    Object[][] result = new Object[length][3];
    int pos = 0;
    for (Object[][] array : arrays) {
      for (Object[] element : array) {
        result[pos] = element;
        pos++;
      }
    }
    return result;
  }

  /**
   * Fuzzer factory which drives properties to the fuzz engine.
   * 
   * @return TestNG Object
   * @throws UnexpectedModelPropertyTypeException
   * @throws IOException
   * @throws ParseException
   * @throws UnexpectedModelDefinitionException
   */
  @Factory
  public Object[] fuzzProperties() throws UnexpectedModelPropertyTypeException {
    Object[] result = new Object[propertiesFuzzSet.length];
    // new Object[totalFuzzSet.length]; //see note about _links properties above
    // for (int i = 0; i < totalFuzzSet.length; i++) {
    for (int i = 0; i < propertiesFuzzSet.length; i++) {
      // String currentkey = totalFuzzSet[i][0].toString();
      String currentkey = propertiesFuzzSet[i][0].toString();
      // ModelPropertyType type = (ModelPropertyType) totalFuzzSet[i][1];
      ModelPropertyType type = (ModelPropertyType) propertiesFuzzSet[i][1];
      Object currentValue = instance.get(currentkey);
      // boolean isLinksProperty = (totalFuzzSet[i][2] == "linksProperty");

      result[i] = new MetadataEngine(targetService, entity, currentkey, type, currentValue,
          requestMethod, contentType, requestBuilder);
    }
    return result;
  }
}
