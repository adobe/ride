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

package com.adobe.ride.libraries.fuzzer.engines;

import java.net.URI;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
// import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import com.adobe.ride.core.RideCore;
import com.adobe.ride.core.controllers.RestApiController;
import com.adobe.ride.libraries.fuzzer.MetadataFuzzer;
import com.adobe.ride.libraries.fuzzer.exceptions.NullPropertyValueException;
import com.adobe.ride.utilities.model.ModelObject;
import com.adobe.ride.utilities.model.exceptions.UnexpectedModelPropertyTypeException;
import com.adobe.ride.utilities.model.types.ModelPropertyType;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.http.Method;
// import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;

/**
 * Engine used by the metadata fuzzer to make calls to the service with .
 * 
 * @author tedcasey
 *
 */
public class MetadataEngine extends CoreEngine {

  public String serviceName;
  public JSONObject modelProperties;
  public JSONObject nodeDefinition;
  public JSONObject modelInstance;
  public JSONArray modelInstanceItems;
  public JSONObject linksInstance;
  public JSONObject customDefs;
  private ModelObject entity;
  public String fuzzPropertyInstancePath;
  public String fuzzProperty;
  private ModelPropertyType propertyType;
  public String entityString;
  public String entityName;
  public Object propertyStartValue;
  private String currentPath;
  private boolean objectExists = false;
  private Method requestMethod;
  private RequestSpecBuilder requestBuilder = null;

  /**
   * Constructor for the class from which all of the fuzz target data is derived.
   * 
   * @param schema Schema of the target service, which is a mapping to the config folder in
   *        the project resources
   * @param entityObj ModelObject to be fuzzed
   * @param parentPath String path to the key in the object
   * @param property Key in the metadata to be fuzzed. Passed by the Metadata Fuzzer
   * @param type ModelPropertyType to be used in more fine grained fuzzing - TBD
   * @param value Start value to be reset in the property to ensure fuzzing is done in isolation
   * @param requestMethod method with which to call the api (if null, default is PUT)
   * @param contentType string content type header value (if null, default is
   *        "application/json;charset=utf-8")
   */
  public MetadataEngine(Schema schema, ModelObject entityObj, String parentPath, String property,
      ModelPropertyType type, Object value, Method requestMethod, String contentType) {
    super(parentPath, property);
    initializeEngine(serviceName, entityObj, parentPath, property, type, value, requestMethod, contentType,
        null);
  }

  /**
   * Constructor for the class from which all of the fuzz target data is derived.
   * 
   * @param schema Schema  of the target service, which is a mapping to the config folder in
   *        the project resources
   * @param entityObj ModelObject to be fuzzed
   * @param parentPath String path to the key in the object
   * @param property Key in the metadata to be fuzzed. Passed by the Metadata Fuzzer
   * @param type ModelPropertyType to be used in more fine grained fuzzing - TBD
   * @param value Start value to be reset in the property to ensure fuzzing is done in isolation
   * @param requestMethod method with which to call the api (if null, default is PUT)
   * @param contentType string content type header value (if null, default is
   *        "application/json;charset=utf-8")
   * @param requestBuilder Rest-Assured request builder
   */
  public MetadataEngine(Schema schema, ModelObject entityObj, String parentPath, String property,
      ModelPropertyType type, Object value, Method requestMethod, String contentType,
      RequestSpecBuilder requestBuilder) {
    super(parentPath, property);
    initializeEngine(serviceName, entityObj, parentPath, property, type, value, requestMethod, contentType,
        requestBuilder);
  }

  /**
   * Constructor for the class from which all of the fuzz target data is derived.
   * 
   * @param schema Schema of the target service, which is a mapping to the config folder in
   *        the project resources
   * @param entityObj ModelObject to be fuzzed
   * @param parentPath String path to the key in the object
   * @param property key in the metadata to be fuzzed. Passed by the MetadataFuzzer
   * @param type ModelPropertyType to be used in more fine grained fuzzing - TBD
   * @param value start value to be reset in the property to ensure fuzzing is done in isolation
   * @param requestMethod http action to be invoked, i.e. POST, GET, etc.  If null, default is PUT
   * @param contentType string content type header value (if null, default is
   *        "application/json;charset=utf-8")
   * @param requestBuilder Rest-Assured RequestSpecBuilder
   * @param filters RestAssured Filters
   */
  public MetadataEngine(Schema schema, ModelObject entityObj, String parentPath, String property,
      ModelPropertyType type, Object value, Method requestMethod, String contentType,
      RequestSpecBuilder requestBuilder, Filter... filters) {
    super(parentPath, property);
    initializeEngine(serviceName, entityObj, parentPath, property, type, value, requestMethod, contentType,
        requestBuilder, filters);
  }
  
  public MetadataEngine(Schema schema, MetadataFuzzer fuzzObj, String parentPath, String property,
      ModelPropertyType type, Object value) {
    super(parentPath, property);
    initializeEngine(serviceName, fuzzObj.entity, parentPath, property, type, value, fuzzObj.requestMethod, fuzzObj.contentType,
        requestBuilder, fuzzObj.filters);
  }

  private void initializeEngine(String serviceName, ModelObject entityObj, String parentPath, String property,
      ModelPropertyType type, Object value, Method requestMethod, String contentType,
      RequestSpecBuilder requestBuilder, Filter... filters) {

    this.serviceName = serviceName;
    this.entity = entityObj;
    this.requestMethod = requestMethod;
    if (requestBuilder != null) {
      this.requestBuilder = requestBuilder;
    } else {
      this.requestBuilder = RestApiController.getRequestBuilder(false);
      this.requestBuilder.addHeader("Accept", "application/json");
      this.requestBuilder.addHeader("Content-Type",
          (contentType != null) ? contentType : "application/json;charset=utf-8");
    }

    requestBuilder = RideCore.nullCheckAndAddFilters(requestBuilder, filters);

    JSONObject model = entity.getModel();
    modelProperties = ModelObject.getModelProperties(entity, model);
    if(entity.getModelType() == ModelPropertyType.OBJECT) {
      modelInstance = entity.getObjectMetadata();
    }else if(entity.getModelType() == ModelPropertyType.ARRAY) {
      modelInstanceItems = entity.getObjectItems();
    }
    fuzzProperty = property;
    fuzzPropertyInstancePath = parentPath;
    propertyType = type;
    propertyStartValue = value;

    nodeDefinition = entity.getDefinitionAtModelPath(modelProperties, parentPath+property);//(JSONObject) modelProperties.get(fuzzProperty);

    if (model.containsKey("definitions")) {
      customDefs = (JSONObject) model.get("definitions");
    }

  }

  /**
   * Method to determine if node is mutable by caller or only by the server (i.e. read/write
   * property or read-only). Non-mutable nodes should be ignored and the call accepted if the 
   * rest of the json is valid.
   * 
   * @param nodeDef node to be tested
   * @return boolean
   */
  private boolean getNodeMutability(JSONObject nodeDef) {
    boolean retVal = true;
    if (nodeDef.containsKey("mutable")) {
      if (Boolean.parseBoolean(nodeDef.get("mutable").toString()) == false) {
        retVal = true;
      } else {
        retVal = false;
      }
    }

    return retVal;
  }

  /**
   * Method which determines whether the fuzz data injected into the metadata node should be allowed.
   * 
   * @param modelDataType - {@link: ModelPropertyType} of the node being fuzzed
   * @param fuzzDataType - {@link: ModelPropertyType} of the data being injected
   * @param data - Object, data being injected
   * @param nodeDef - full JSON schema definition of the node.
   * @return boolean
   */
  private boolean getSuccessExpectation(ModelPropertyType modelDataType,
      ModelPropertyType fuzzDataType, Object data, JSONObject nodeDef) {
    boolean retVal = false;

    if (!getNodeMutability(nodeDef)) {// non-mutable nodes should be ignored
      // and the call accepted if the rest
      // of the json is valid
      retVal = true;
    } else {

      if (modelDataType.equals(ModelPropertyType.URI)
          && (fuzzDataType.equals(ModelPropertyType.STRING) && nodeDef.containsKey("format"))) {
        if (nodeDef.get("format").toString().equals("uri")
            || nodeDef.get("format").toString().equals("uri-reference")) {
          retVal = validateURI(data.toString());
        }
      } else if (modelDataType.equals(ModelPropertyType.STRING)
          && fuzzDataType.equals(ModelPropertyType.STRING) && !nodeDef.containsKey("format")) {
        retVal = validateStr(data.toString(), nodeDef);
      } else if (modelDataType.equals(ModelPropertyType.INTEGER)
          && (fuzzDataType.equals(ModelPropertyType.INTEGER)
              || fuzzDataType.equals(ModelPropertyType.BYTE))) {
        // for comparison purposes, byte is seen as an integer.
        if (!isUnsetNumberDef(nodeDef)) {
          retVal = validateInt(Integer.parseInt(data.toString()), nodeDef);
        } else {
          retVal = false;
        }
      } else if (modelDataType.equals(ModelPropertyType.NUMBER)
          && (fuzzDataType.equals(ModelPropertyType.NUMBER)
              || fuzzDataType.equals(ModelPropertyType.INTEGER)
              || fuzzDataType.equals(ModelPropertyType.BYTE))) {
        // treat JSON number type as float.
        if (!isUnsetNumberDef(nodeDef)) {
          retVal = validateFloat(Float.parseFloat(data.toString()), nodeDef);
        }
      } else if (fuzzDataType.equals(modelDataType)) {
        retVal = true;
      }
    }
    return retVal;
  }

  /**
   * Determine, for Integer node defs, if a "null" value (i.e. -1) is allowed.
   * 
   * @param nodeDef JSON Definition of the schema node
   * @return boolean
   */
  private boolean isUnsetNumberDef(JSONObject nodeDef) {
    boolean retVal = false;
    if (nodeDef.containsKey("minimum") && nodeDef.containsKey("maximum")) {
      if (Integer.parseInt(nodeDef.get("minimum").toString()) < 0
          && Integer.parseInt(nodeDef.get("maximum").toString()) < 0) {
        retVal = true;
      }
    }
    return retVal;
  }

  /**
   * Return value of boolean property in node def, if it exists.
   * 
   * @param nodeDef full JSON schema node definition
   * @param key String value of the key to be looked up
   * @return boolean
   */
  private boolean returnBooleanKeyValue(JSONObject nodeDef, String key) {
    boolean retVal = false;
    if (nodeDef.containsKey(key)) {
      retVal = Boolean.parseBoolean(nodeDef.get(key).toString());
    }
    return retVal;
  }

  /**
   * For Nodes which are of type int, determine if the fuzz int value passed conforms to the
   * definition.
   * 
   * @param fuzzValue Int value
   * @param nodeDef Full JSON schema definition for the node
   * @return boolean
   */
  private boolean validateInt(int fuzzValue, JSONObject nodeDef) {
    boolean retVal = true;
    boolean exclusiveMin = returnBooleanKeyValue(nodeDef, "exclusiveMin");
    boolean exclusiveMax = returnBooleanKeyValue(nodeDef, "exclusiveMmax");

    if (nodeDef.containsKey("minimum")) {
      int min = Integer.parseInt(nodeDef.get("minimum").toString());

      retVal = validateIntLimit(min, fuzzValue, false, exclusiveMin);
    }

    if (retVal == true && nodeDef.containsKey("maximum")) {
      int max = Integer.parseInt(nodeDef.get("maximum").toString());
      retVal = validateIntLimit(max, fuzzValue, true, exclusiveMax);
    }

    return retVal;
  }

  /**
   * For Nodes which are of type Number, determine if the fuzz value passed conforms to the
   * definition For memory purposes, treating all numbers as floats.
   * 
   * @param fuzzValue float value
   * @param nodeDef Full JSON schema definition for the node
   * @return
   */
  private boolean validateFloat(float fuzzValue, JSONObject nodeDef) {
    boolean retVal = true;
    boolean exclusiveMin = returnBooleanKeyValue(nodeDef, "exclusiveMin");
    boolean exclusiveMax = returnBooleanKeyValue(nodeDef, "exclusiveMmax");

    if (nodeDef.containsKey("minimum")) {
      float min = Float.parseFloat(nodeDef.get("minimum").toString());

      retVal = validateFloatLimit(min, fuzzValue, false, exclusiveMin);
    }

    if (retVal == true && nodeDef.containsKey("maximum")) {
      int max = Integer.parseInt(nodeDef.get("maximum").toString());
      retVal = validateFloatLimit(max, fuzzValue, true, exclusiveMax);
    }

    return retVal;
  }

  /**
   * For node types of ANYOF, loop through them to see if the fuzz data conforms any of the
   * definition types.
   * 
   * @param dataType Type of the fuzz data being injected
   * @param data fuzz data
   * @return boolean
   */
  private boolean loopAnyOf(ModelPropertyType dataType, Object data) {
    boolean retVal = true;

    JSONArray array = (JSONArray) nodeDefinition.get("anyOf");
    for (int i = 0; i < array.size(); i++) {
      JSONObject obj = (JSONObject) array.get(i);
      ModelPropertyType type = null;
      try {
        type = ModelObject.getModelPropertyType(obj);
      } catch (UnexpectedModelPropertyTypeException e) {
        System.out
            .println("FUZZER ERROR - UnexpectedModelPropertyTypeException: " + e.getMessage());
        e.printStackTrace();
      }
      retVal = getSuccessExpectation(type, dataType, data, obj);
      if (retVal) {
        break;
      }
    }
    return retVal;
  }

  /**
   * For nodes of type String, determine if the String passed in conforms to the schema definition.
   * 
   * @param str String fuzz data
   * @param nodeDef Definition of the schema node
   * @return boolean
   */
  private boolean validateStr(String str, JSONObject nodeDef) {
    boolean retVal = true;
    int length = str.length();
    boolean exclusiveMin = returnBooleanKeyValue(nodeDef, "exclusiveMin");
    boolean exclusiveMax = returnBooleanKeyValue(nodeDef, "exclusiveMmax");

    if (nodeDef.containsKey("minLength")) {
      int min = Integer.parseInt(nodeDef.get("minLength").toString());
      retVal = validateIntLimit(min, length, false, exclusiveMin);
    }

    if (retVal == true && nodeDef.containsKey("maxLength")) {
      int max = Integer.parseInt(nodeDef.get("maxLength").toString());
      retVal = validateIntLimit(max, length, true, exclusiveMax);
    }

    return retVal;
  }

  /**
   * Determine if a passed int value conforms to parameters passed in.
   * 
   * @param limit int limit value.
   * @param val int value to be analyzed
   * @param max boolean indicating if the limit is an upper limit or a lower limit
   * @param exclusive boolean indicating if the limit is inclusive or exclusive
   * @return boolean
   */
  private boolean validateIntLimit(int limit, int val, boolean max, boolean exclusive) {
    boolean retValue = true;
    if (max) {
      limit = (exclusive) ? limit - 1 : limit;
      retValue = val <= limit;
    } else {
      limit = (exclusive) ? limit + 1 : limit;
      retValue = val >= limit;
    }
    return retValue;
  }

  /**
   * Determine if a passed float value conforms to paramters passed in.
   * 
   * @param limit float limit value
   * @param val float value to be analyzed
   * @param max boolean indicating if the limit is an upper limit or a lower limit
   * @param exclusive boolean indicating if the limit is inclusive or exclusive
   * @return boolean
   */
  private boolean validateFloatLimit(float limit, float val, boolean max, boolean exclusive) {
    boolean retValue = true;
    float incrementor = Float.parseFloat("0.1");
    if (max) {
      limit = (exclusive) ? limit - incrementor : limit;
      retValue = val <= limit;
    } else {
      limit = (exclusive) ? limit + incrementor : limit;
      retValue = val >= limit;
    }
    return retValue;
  }

  /**
   * For nodes of type URI, determine if the fuzz value is a valid URI
   * 
   * @param str
   * @return
   */
  private boolean validateURI(String str) {
    try {
      new URI(str);
    } catch (Exception e1) {
      return false;
    }
    return true;
  }

  /**
   * Method to determine whether fuzz data conforms to the node definition
   * 
   * @param dataType ModelPropertyType of the fuzzed data
   * @param data value of the fuzzed data
   * @return boolean
   */
  private boolean evalData(ModelPropertyType dataType, Object data) {
    boolean retVal = false;
    if (propertyType == ModelPropertyType.ANYOF) {
      retVal = loopAnyOf(dataType, data);
    } else if (getSuccessExpectation(propertyType, dataType, data, nodeDefinition)) {
      retVal = true;
    } else {
      retVal = false;
    }
    return retVal;
  }

  /**
   * Method which makes the call to the service and validates the result.
   * 
   * @param propertyValue
   * @param expectSuccess boolean to determine whether the call should succeed or fail
   */
  private Response callService(Object propertyValue, boolean expectSuccess) {

    entity.setMetadataValue(fuzzPropertyInstancePath, fuzzProperty, propertyValue);
    
    String callBody = entity.getMetadataString();
    requestBuilder.setBody(callBody);

    ResponseSpecBuilder expectedValues = new ResponseSpecBuilder();

    if(expectSuccess) {
      
      if (requestMethod == Method.DELETE) {
        expectedValues.expectStatusCode(204);
      } else if (requestMethod == Method.GET) {
        expectedValues.expectStatusCode(200);
      } else if (requestMethod == Method.POST) {
        if (objectExists) {
          // Not sure about this one. May need another global variable about whether or not to
          // expect an overwrite
          expectedValues.expectStatusCode(409);
        } else {
          expectedValues.expectStatusCode(201);
          objectExists = true;
        }
      } else if (requestMethod == Method.PUT) {
        if (objectExists) {
          expectedValues.expectStatusCode(200);
        } else {
          expectedValues.expectStatusCode(201);
          objectExists = true;
        }
      }
    } else {
      expectedValues.expectStatusCode(400);
    }

    ResponseSpecification expectedResponse = expectedValues.build();
    currentPath = entity.getObjectPath();

    Method method = (requestMethod != null) ? requestMethod : Method.PUT;

    return RestApiController.fireRestCall(serviceName, currentPath, requestBuilder,
        expectedResponse, method);
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param path --
   * @param property --
   * @param propertyValue --
   */
  @Test(dataProvider = "nonStringsDP", suiteName = "fuzzer", groups = "fuzz", enabled = false,
      singleThreaded = true)
  public void fuzzMetadataStringsWithNonStrings(String path, String property,
      Object propertyValue) {

    String javaType = propertyValue.getClass().getSimpleName();
    ModelPropertyType type;
    if (javaType.equals("Double") || javaType.equals("Float")) {
      type = ModelPropertyType.NUMBER;
    } else {
      type = ModelPropertyType.eval(javaType);
    }
    boolean expectSuccess = evalData(type, propertyValue);
    Response response = callService(propertyValue, expectSuccess);
    validateResult(property, propertyValue, response, expectSuccess);
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param path --
   * @param property --
   * @param propertyValue --
   */
  @Test(dataProvider = "localizedStringsDP", suiteName = "fuzzer", groups = "fuzz", enabled = true,
      singleThreaded = true)
  public void fuzzMetadataStringsWithLocalizedStrings(String path, String property,
      Object propertyValue) {
    boolean expectSuccess = evalData(ModelPropertyType.STRING, propertyValue);
    Response response = callService(propertyValue, expectSuccess);
    validateResult(property, propertyValue, response, expectSuccess);
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param path --
   * @param property --
   * @param propertyValue --
   */
  @Test(dataProvider = "passiveSqlDP", suiteName = "fuzzer", groups = "fuzz", enabled = true,
      singleThreaded = true)
  public void fuzzMetadataStringsWithPassiveSQLInjectionStrings(String path, String property,
      Object propertyValue) {
    boolean expectSuccess = evalData(ModelPropertyType.STRING, propertyValue);
    Response response = callService(propertyValue, expectSuccess);
    validateResult(property, propertyValue, response, expectSuccess);
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param path --
   * @param property --
   * @param propertyValue --
   */
  @Test(dataProvider = "noSqlDP", groups = "fuzz", enabled = true,
      singleThreaded = true)
  public void fuzzMetadataStringsWithNoSQLInjectionStrings(String path, String property,
      Object propertyValue) {
    boolean expectSuccess = evalData(ModelPropertyType.STRING, propertyValue);
    Response response = callService(propertyValue, expectSuccess);
    validateResult(property, propertyValue, response, expectSuccess);
  }

  /**
   * Resets the start value for the fuzzed property before moving on the the next property to be
   * fuzzed.
   */
  @AfterMethod
  private void resetValue() throws NullPropertyValueException{

    if (propertyStartValue != null) {
      entity.setMetadataValue(fuzzPropertyInstancePath, fuzzProperty, propertyStartValue);
    } else {
      throw new NullPropertyValueException(modelInstance, fuzzProperty);
      
    }
    
  }
}
