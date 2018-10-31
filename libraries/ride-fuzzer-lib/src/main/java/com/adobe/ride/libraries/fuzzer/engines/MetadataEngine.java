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

import java.io.IOException;
import java.net.URI;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
// import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.adobe.ride.core.controllers.RestApiController;
import com.adobe.ride.utilities.model.ModelObject;
import com.adobe.ride.utilities.model.exceptions.UnexpectedModelPropertyTypeException;
import com.adobe.ride.utilities.model.types.ModelPropertyType;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
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
  public JSONObject linksProperties;// For HAL-style schema handling
  public JSONObject nodeDefinition;
  public JSONObject modelInstance;
  public JSONObject linksInstance;
  public JSONObject customDefs;
  private boolean linksProperty;
  private ModelObject entity;
  public String modelProperty;
  private ModelPropertyType propertyType;
  public String entityString;
  public String entityName;
  public Object propertyStartValue;
  private String currentPath;
  private boolean objectExists = false;
  private Method requestMethod;
  private RequestSpecBuilder requestBuilder = null;
  private Schema schema;

  /**
   * Constructor for the class from which all of the fuzz target data is derived.
   * 
   * @param entityObj {@link: EntityObject} to be fuzzed
   * @param property Key in the metadata to be fuzzed. Passed by the Metadata Fuzzer.
   * @param type {@link: ModelPropertyType} to be used in more fine grained fuzzing - TBD
   * @param value Start value to be reset in the property to ensure fuzzing is done in isolation.
   * @param requestMethod method with which to call the api (if null, default is PUT)
   * @param contentType string content type header value (if null, default is
   *        "application/json;charset=utf-8")
   * @throws UnexpectedModelPropertyTypeException
   */
  public MetadataEngine(String serviceName, ModelObject entityObj, String property,
      ModelPropertyType type, Object value, Method requestMethod, String contentType) {
    super(entityObj.getModelString(), property);
    initializeEngine(serviceName, entityObj, property, type, value, requestMethod, contentType,
        null);
  }

  public MetadataEngine(String serviceName, ModelObject entityObj, String property,
      ModelPropertyType type, Object value, Method requestMethod, String contentType,
      RequestSpecBuilder requestBuilder) {
    super(entityObj.getModelString(), property);
    initializeEngine(serviceName, entityObj, property, type, value, requestMethod, contentType,
        requestBuilder);
  }

  private void initializeEngine(String serviceName, ModelObject entityObj, String property,
      ModelPropertyType type, Object value, Method requestMethod, String contentType,
      RequestSpecBuilder requestBuilder) {

    this.serviceName = serviceName;
    this.entity = entityObj;
    this.requestMethod = requestMethod;
    if (requestBuilder != null) {
      this.requestBuilder = requestBuilder;
    } else {
      this.requestBuilder =
          initializeFuzzerRequestSpecBldr(RestApiController.getRequestBuilder(false));
      this.requestBuilder.addHeader("Accept", "application/json");
      this.requestBuilder.addHeader("Content-Type",
          (contentType != null) ? contentType : "application/json;charset=utf-8");
    }
    JSONObject model = entity.getModel();
    modelProperties = (JSONObject) model.get("properties");
    linksProperties = (modelProperties.containsKey("_links"))
        ? (JSONObject) ((JSONObject) modelProperties.get("_links")).get("properties")
        : null;

    modelInstance = (JSONObject) entity.buildValidModelInstance();
    modelProperty = property;
    propertyType = type;
    propertyStartValue = value;

    if (linksProperty) {
      linksInstance = (JSONObject) modelInstance.get("_links");
    }

    if (linksProperty) {
      nodeDefinition = (JSONObject) linksProperties.get(modelProperty);
    } else {
      nodeDefinition = (JSONObject) modelProperties.get(modelProperty);
    }
    nodeDefinition = (JSONObject) modelProperties.get(modelProperty);

    if (model.containsKey("definitions")) {
      customDefs = (JSONObject) model.get("definitions");
    }

    org.json.JSONObject rawSchema = new org.json.JSONObject(entity.getModelString());
    schema = SchemaLoader.load(rawSchema);
  }

  /**
   * Method to determine if node is mutable by caller or only by the server (i.e. read/write
   * property or read-only)
   * 
   * @param nodeDef
   * @return
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
   * Method which determines whether the fuzz data injected into the metadata node should be allowed
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
        if (nodeDef.get("format").toString().equals("uri") || nodeDef.get("format").toString().equals("uri-reference")) {
          // TODO: ModelObject does handle uri format need to investigate and possibly remove this
          // conditional
          retVal = validateURI(data.toString());
        }
      } else if (modelDataType.equals(ModelPropertyType.STRING)
          && fuzzDataType.equals(ModelPropertyType.STRING) && !nodeDef.containsKey("format")) {
        // TODO: I believe this conditional is no longer needed. Need to investigate
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
   * @param nodeDef JSON Definition of the schema node.
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
   * @param nodeDef full JSON schema node definition.
   * @param key String value of the key to be looked up.
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
   * definition
   * 
   * @param fuzzValue Int value
   * @param nodeDef Full JSON schema definition for the node.
   * @return
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
   * @param nodeDef Full JSON schema definition for the node.
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
   * @param dataType Type of the fuzz data being injected.
   * @param data fuzz data
   * @return boolean
   * @throws UnexpectedModelPropertyTypeException
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
   * @param str String fuzz data.
   * @param nodeDef Definition of the schema node.
   * @return Boolean
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
   * @param val int value to be analyzed.
   * @param max boolean indicating if the limit is an upper limit or a lower limit
   * @param exclusive boolean indicating if the limit is inclusive or exclusive.
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
   * @param limit float limit value.
   * @param val float value to be analyzed.
   * @param max boolean indicating if the limit is an upper limit or a lower limit
   * @param exclusive boolean indicating if the limit is inclusive or exclusive.
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
   * @param dataType ModelPropertyType of the fuzzed data.
   * @param data value of the fuzzed data.
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
  @SuppressWarnings("unchecked")
  private Response callService(Object propertyValue, boolean expectSuccess) {

    if (linksProperty) {
      setLinksProperty(propertyValue);
    } else {
      modelInstance.remove(modelProperty);
      modelInstance.put(modelProperty, propertyValue);
    }

    String callBody = modelInstance.toJSONString();
    requestBuilder.setBody(callBody);

    ResponseSpecBuilder expectedValues = new ResponseSpecBuilder();

    

    try {
      org.json.JSONObject objectMetadata = new org.json.JSONObject(callBody);
      schema.validate(objectMetadata);

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
    } catch (ValidationException e) {
      expectedValues.expectStatusCode(400);
    }

    ResponseSpecification expectedResponse = expectedValues.build();
    currentPath = entity.getObjectPath();

    Method method = (requestMethod != null) ? requestMethod : Method.PUT;

    switch (method) {
      case DELETE:
        return RestApiController.delete(serviceName, currentPath, requestBuilder, expectedResponse);
      case GET:
        return RestApiController.get(serviceName, currentPath, requestBuilder, expectedResponse);
      case HEAD:
        return RestApiController.head(serviceName, currentPath, requestBuilder, expectedResponse);
      case OPTIONS:
        return RestApiController.options(serviceName, currentPath, requestBuilder,
            expectedResponse);
      case PATCH:
        return RestApiController.patch(serviceName, currentPath, requestBuilder, expectedResponse);
      case POST:
        return RestApiController.post(serviceName, currentPath, requestBuilder, expectedResponse);
      case PUT:
        return RestApiController.put(serviceName, currentPath, requestBuilder, expectedResponse);
      default:
        return null;
    }

    // resetVersion(response);TODO: Might need to implement this a new way

  }

  /*
   * private void resetVersion(Response response){ if(response.getStatusCode() == 201 ||
   * response.getStatusCode() == 200) { currentVersion =
   * response.body().jsonPath().getString("version"); entity.setProperty("version", currentVersion);
   * entity.setLastRetrievedVersion(currentVersion); } }
   */

  /**
   * Method to set the links property being tested.
   * 
   * @param propertyValue
   */
  @SuppressWarnings("unchecked")
  private void setLinksProperty(Object propertyValue) {
    JSONObject tempLinks = linksInstance;
    tempLinks.remove(modelProperty);
    tempLinks.put(modelProperty, propertyValue);
    modelInstance.remove("_links");
    modelInstance.put("_links", tempLinks);
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param entityType
   * @param property property being fuzzed
   * @param propertyValue fuzz data injected
   */
  @Test(dataProvider = "nonStringsDP", suiteName = "fuzzer", groups = "fuzz", enabled = false,
      singleThreaded = true)
  public void fuzzMetadataStringsWithNonStrings(String entityType, String property,
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
   * @param entityType
   * @param property
   * @param propertyValue
   */
  @Test(dataProvider = "localizedStringsDP", suiteName = "fuzzer", groups = "fuzz", enabled = true,
      singleThreaded = true)
  public void fuzzMetadataStringsWithLocalizedStrings(String entityType, String property,
      Object propertyValue) {
    boolean expectSuccess = evalData(ModelPropertyType.STRING, propertyValue);
    Response response = callService(propertyValue, expectSuccess);
    validateResult(property, propertyValue, response, expectSuccess);
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param entityType
   * @param property
   * @param propertyValue
   */
  @Test(dataProvider = "passiveSqlDP", suiteName = "fuzzer", groups = "fuzz", enabled = true,
      singleThreaded = true)
  public void fuzzMetadataStringsWithPassiveSQLInjectionStrings(String entityType, String property,
      Object propertyValue) {
    boolean expectSuccess = evalData(ModelPropertyType.STRING, propertyValue);
    Response response = callService(propertyValue, expectSuccess);
    validateResult(property, propertyValue, response, expectSuccess);
  }

  /**
   * DO NOT CALL THIS METHOD DIRECTLY. This is an internal method that is only public because TestNG
   * requires it. Test method which uses a TestNG DP to fuzz the target.
   * 
   * @param entityType
   * @param property
   * @param propertyValue
   */
  @Test(dataProvider = "noSqlDP", suiteName = "fuzzer", groups = "fuzz", enabled = true,
      singleThreaded = true)
  public void fuzzMetadataStringsWithNoSQLInjectionStrings(String entityType, String property,
      Object propertyValue) {
    boolean expectSuccess = evalData(ModelPropertyType.STRING, propertyValue);
    Response response = callService(propertyValue, expectSuccess);
    validateResult(property, propertyValue, response, expectSuccess);
  }

  /**
   * Resets the start value for the fuzzed property before moving on the the next property to be
   * fuzzed.
   * 
   * @throws UnexpectedModelPropertyTypeException
   * 
   * @throws ParseException
   */
  @SuppressWarnings("unchecked")
  @AfterMethod
  private void resetValue() throws UnexpectedModelPropertyTypeException {

    if (propertyStartValue != null) {
      modelInstance.remove(modelProperty);
      modelInstance.put(modelProperty, propertyStartValue);
    } else if (linksProperty) {
      // for links properties, and perhaps just
      // array properties, attempting to reset to
      // the original value fails, most
      // likely due to cast issues, so regenerating
      // a valid value.
      Object nodeValue = null;
      nodeValue = entity.generateNodeValue(nodeDefinition);
      setLinksProperty(nodeValue);
    } else {
      Object nodeValue = null;
      nodeValue = entity.generateNodeValue(nodeDefinition);
      modelInstance.remove(modelProperty);
      modelInstance.put(modelProperty, nodeValue);
    }
  }
}
