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

import org.json.simple.JSONObject;
import org.testng.annotations.Factory;
import com.adobe.ride.libraries.fuzzer.engines.MetadataEngine;
import com.adobe.ride.utilities.model.ModelObject;
import com.adobe.ride.utilities.model.exceptions.UnexpectedModelPropertyTypeException;
import com.adobe.ride.utilities.model.types.ModelPropertyType;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.Filter;
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
  protected String serviceName;
  protected Method requestMethod;
  protected String contentType;
  protected RequestSpecBuilder requestBuilder;
  protected Filter[] filters;

  /** Arrays to be dynamically populated with metadata info for fuzzing **/
  private Object[][] propertiesFuzzSet;

  /**
   * Method to initialize the Fuzzer factory and related engine subcomponents.
   * 
   * @param serviceName name of the target service, which is a mapping to the config folder in
   *        the project resources
   * @param objectToBeFuzzed ModelObject or a subclass thereof
   */
  public MetadataFuzzer(String serviceName, ModelObject objectToBeFuzzed) {
    initializeFuzzer(serviceName, objectToBeFuzzed, null, null, null);
  }

  /**
   * Method to initialize the Fuzzer factory and related engine subcomponents.
   * 
   * @param serviceName name of the target service, which is a mapping to the config folder in the
   *        project resources
   * @param objectToBeFuzzed ModelObject or a subclass thereof
   * @param builder RequestSpecBuilder to be used in the fuzzer calls. If null, a base spec with
   *        only Authorization, Content-Type, and Accepts set, with the latter two set to a json
   *        variant
   */
  public MetadataFuzzer(String serviceName, ModelObject objectToBeFuzzed,
      RequestSpecBuilder builder) {
    initializeFuzzer(serviceName, objectToBeFuzzed, builder, null, null);
  }

  /**
   * Method to initialize the Fuzzer factory and related engine subcomponents.
   * 
   * @param serviceName name of the target service, which is a mapping to the config folder in
   *        the project resources
   * @param objectToBeFuzzed ModelObject or a subclass thereof
   * @param builder RequestSpecBuilder to be used in the fuzzer calls. If null, a base spec with
   *        only Authorization, Content-Type, and Accepts set, with the latter two set to a json
   *        variant
   * @param method Method http action to be invoked (i.e. POST, GET, etc.). If null, PUT is used.
   */
  public MetadataFuzzer(String serviceName, ModelObject objectToBeFuzzed,
      RequestSpecBuilder builder, Method method) {
    initializeFuzzer(serviceName, objectToBeFuzzed, builder, method, null);
  }

  /**
   * Method to initialize the Fuzzer factory and related engine subcomponents.
   * 
   * @param serviceName name of the target service, which is a mapping to the config folder in
   *        the project resources
   * @param objectToBeFuzzed ModelObject or a subclass thereof
   * @param builder RequestSpecBuilder to be used in the fuzzer calls. If null, a base spec with
   *        only Authorization, Content-Type, and Accepts set, with the latter two set to a json
   *        variant
   * @param method Method http action to be invoked (i.e. POST, GET, etc.). If null, PUT is used
   * @param contentType String content type header value (if null, default is
   *        "application/json;charset=utf-8")
   */
  public MetadataFuzzer(String serviceName, ModelObject objectToBeFuzzed,
      RequestSpecBuilder builder, Method method, String contentType) {
    initializeFuzzer(serviceName, objectToBeFuzzed, builder, method, contentType);
  }

  /**
   * Method to initialize the Fuzzer factory and related engine subcomponents.
   * 
   * @param serviceName name of the target service, which is a mapping to the config folder in
   *        the project resources
   * @param objectToBeFuzzed ModelObject or a subclass thereof
   * @param builder RequestSpecBuilder to be used in the fuzzer calls. If null, a base spec with
   *        only Authorization, Content-Type, and Accepts set, with the latter two set to a json
   *        variant
   * @param method Method http action to be invoked (i.e. POST, GET, etc.). If null, PUT is used
   * @param contentType string content type header value (if null, default is
   *        "application/json;charset=utf-8")
   * @param filter Filter to be applied to calls.
   */
  public MetadataFuzzer(String serviceName, ModelObject objectToBeFuzzed,
      RequestSpecBuilder builder, Method method, String contentType, Filter filter) {
    initializeFuzzer(serviceName, objectToBeFuzzed, builder, method, contentType, filter);
  }

  /**
   * Method to initialize the Fuzzer factory and related engine subcomponents.
   * 
   * @param serviceName name of the target service, which is a mapping to the config folder in
   *        the project resources
   * @param objectToBeFuzzed ModelObject subclass instance to be fuzzed.
   * @param method http action to be invoked (i.e. POST, GET, etc.). If null, PUT is used
   * @param contentType content type header value (if null, default is
   *        "application/json;charset=utf-8")
   */
  public MetadataFuzzer(String serviceName, ModelObject objectToBeFuzzed, Method method,
      String contentType) {
    initializeFuzzer(serviceName, objectToBeFuzzed, null, method, contentType);
  }

  private void initializeFuzzer(String serviceName, ModelObject objectToBeFuzzed,
      RequestSpecBuilder requestBuilder, Method method, String contentType, Filter... filters) {

    // Populate global properties
    this.entity = objectToBeFuzzed;
    this.requestMethod = method;
    this.serviceName = serviceName;
    this.properties = this.entity.getModelProperties();
    this.instance = this.entity.getObjectMetadata();
    this.requestMethod = method;
    this.contentType = contentType;
    this.requestBuilder = requestBuilder;
    this.filters = filters;
    propertiesFuzzSet = new Object[properties.keySet().size()][3];

    // Prep TestNG dataprovider to make reporting better.
    populateFuzzSet(properties, propertiesFuzzSet, "rootProperty");
  }

  /**
   * Method which turns the properties of the model into a dataprovider for the fuzzer to use.
   * 
   * @param set JSONObject representing a set of metadata nodes
   * @param fuzzSet the array in which the data will be pushed
   * @param propertyType indicator specifying links or root property
   * @throws UnexpectedModelPropertyTypeException
   */
  private void populateFuzzSet(JSONObject set, Object[][] fuzzSet, String propertyType) {
    try {
      JSONObject newSet = getFullObject(set);
      Object[] keys = newSet.keySet().toArray();

      for (int i = 0; i < keys.length; i++) {
        JSONObject obj = (JSONObject) set.get(keys[i]);
        ModelPropertyType type = ModelObject.getModelPropertyType(obj);

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
   * Method to modify json such that all the child objects are added on parent level
   * @param JSON object containing child objects
   * @return Modified JSON object by adding child objects on parent level and deleting from original positions
   */
  private JSONObject getFullObject(JSONObject object) {
	Object[] keys = object.keySet().toArray();
	for (int i = 0; i < keys.length; i++) {
	  JSONObject subObject = (JSONObject) object.get(keys[i]);
	  try {
		ModelPropertyType typeSubObject = ModelObject.getModelPropertyType(subObject);
		if (typeSubObject == ModelPropertyType.OBJECT) {
		  object.remove(keys[i]);
		  object.putAll(getFullObject((JSONObject)subObject.get("properties")));
		}
	  }
	  catch (UnexpectedModelPropertyTypeException e) {
		e.printStackTrace();
	  }
	}
	return object;
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
   * @return Object[]
   */
  @Factory
  public Object[] fuzzProperties() {
    Object[] result = new Object[propertiesFuzzSet.length];
    for (int i = 0; i < propertiesFuzzSet.length; i++) {
      String currentkey = propertiesFuzzSet[i][0].toString();
      ModelPropertyType type = (ModelPropertyType) propertiesFuzzSet[i][1];
      Object currentValue = instance.get(currentkey);

      result[i] = new MetadataEngine(serviceName, entity, currentkey, type, currentValue,
          requestMethod, contentType, requestBuilder, filters);
    }
    return result;
  }
}
