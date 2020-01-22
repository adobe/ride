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

import java.io.File;
import java.util.ArrayList;
//import org.everit.json.schema.Schema;
//import org.everit.json.schema.loader.SchemaLoader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.Factory;
//import com.adobe.ride.core.RideCore;
import com.adobe.ride.core.controllers.RestApiController;
import com.adobe.ride.libraries.fuzzer.engines.MetadataEngine;
import com.adobe.ride.utilities.model.ModelObject;
import com.adobe.ride.utilities.model.exceptions.ModelSearchException;
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
  public ModelObject entity;
  protected JSONObject properties;
  public String serviceName;
  public Method requestMethod;
  public String contentType;
  public RequestSpecBuilder requestBuilder;
  public Filter[] filters;
  protected String root;

  /** Arrays to be dynamically populated with metadata info for fuzzing **/
  private Object[][] propertiesFuzzSet;

  /**
   * Method to initialize the Fuzzer factory and related engine subcomponents.
   * 
   * @param serviceName name of the target service, which is a mapping to the config folder in the
   *        project resources
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
   * @param serviceName name of the target service, which is a mapping to the config folder in the
   *        project resources
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
   * @param serviceName name of the target service, which is a mapping to the config folder in the
   *        project resources
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
   * @param serviceName name of the target service, which is a mapping to the config folder in the
   *        project resources
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
   * @param serviceName name of the target service, which is a mapping to the config folder in the
   *        project resources
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
    this.entity.buildValidModelInstance();

    this.requestMethod = method;
    this.serviceName = serviceName;
    this.properties = ModelObject.getObjectNodeProperties(entity, this.entity.getModel());
    this.requestMethod = method;
    this.contentType = contentType;
    this.requestBuilder = requestBuilder;
    
    if (requestBuilder != null) {
      this.requestBuilder = requestBuilder;
    } else {
      this.requestBuilder = RestApiController.getRequestBuilder(false);
      this.requestBuilder.addHeader("Content-Type", "application/json");
    }

    this.filters = filters;

    // Prep TestNG dataprovider to make reporting better.
    root = (objectToBeFuzzed.getModelType() == ModelPropertyType.OBJECT) ? File.separator
        : File.separator + "0" + File.separator ;

    propertiesFuzzSet = populateFuzzSet(this.entity, new ArrayList<Object[]>(), root, this.entity.getModel());

    for (int i = 0; i < propertiesFuzzSet.length; i++) {
      System.out.println(propertiesFuzzSet[i][0] + ", " + propertiesFuzzSet[i][1] + ", "
          + propertiesFuzzSet[i][2]);
    }
    
    ModelPropertyType type = entity.getModelType();
    if (type == ModelPropertyType.ARRAY) {
      ModelObject.prettyPrintToConsole(entity.getObjectItems());
    } else {
      ModelObject.prettyPrintToConsole(entity.getObjectMetadata());
    }
  }


  private String getFuzzPath(JSONObject currentPathModel, String currentPath, String propertyName) {
    String fuzzPath = "";
    try {
      ModelPropertyType type = ModelObject.getModelPropertyType(currentPathModel);
      if (type == ModelPropertyType.ARRAY) {
        fuzzPath = currentPath + propertyName + File.separator + "0" + File.separator;
      } else {
        fuzzPath = currentPath + propertyName + File.separator;
      }
    } catch (UnexpectedModelPropertyTypeException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return fuzzPath;
  }
  
  private class ModelProperties{
    Object[] propertiesArray;
    JSONObject properties;
    
    public ModelProperties(JSONObject properties) {
      this.properties = properties;
      this.propertiesArray = properties.keySet().toArray();
    }
  }

  private ModelProperties getObjectProperties(ModelObject currentfuzzObject,
      JSONObject currentPathModel) {
    JSONObject properties = null;

    properties = ModelObject.getObjectNodeProperties(currentfuzzObject, currentPathModel);

    return new ModelProperties(properties);
  }

  private boolean isObjectProperty(ModelPropertyType type) {
    if (type == ModelPropertyType.OBJECT || type == ModelPropertyType.ARRAY
        || type == ModelPropertyType.REF_SCHEMA || type == ModelPropertyType.REF_DEFINITION) {
      return true;
    } else {
      return false;
    }
  }


  /**
   * Method which turns the properties of the model into a dataprovider for the fuzzer to use.
   * 
   * @param set JSONObject representing a set of metadata nodes
   * @param fuzzSet the array in which the data will be pushed
   * @param fuzzPropertyType indicator specifying links or root property
   * @throws UnexpectedModelPropertyTypeException
   */
  private Object[][] populateFuzzSet(ModelObject objectToBeFuzzed,
      ArrayList<Object[]> fuzzPropertiesList, String currentPath, JSONObject currentPathModel) {
    Object[][] returnArray = null;
    try {
      ModelProperties modelProps = getObjectProperties(objectToBeFuzzed, currentPathModel);
      Object[] propertiesArray = modelProps.propertiesArray;
      JSONObject properties = modelProps.properties;
      for (int i = 0; i < propertiesArray.length; i++) {
        String propertyName = propertiesArray[i].toString();
        
        JSONObject propertyDef = (JSONObject) properties.get(propertyName);
        ModelPropertyType type = ModelObject.getModelPropertyType(propertyDef);
        if (isObjectProperty(type)) {
          populateFuzzSet(objectToBeFuzzed,
              fuzzPropertiesList, getFuzzPath(propertyDef, currentPath, propertyName),
              propertyDef);
        } else {
          Object[] propertyFuzzSet = new Object[3];
          propertyFuzzSet[0] = currentPath;
          propertyFuzzSet[1] = propertyName;
          propertyFuzzSet[2] = (ModelPropertyType) type;

          fuzzPropertiesList.add(propertyFuzzSet);
        }
      }

      if (currentPath == root) {
        returnArray = convertFuzzListToArray(fuzzPropertiesList);
      }
    } catch (UnexpectedModelPropertyTypeException e) {
      System.out.println("FUZZER ERROR: UnexpectedModelPropertyType:" + e.getMessage());
      e.printStackTrace();
    }
    return returnArray;
  }

  private Object[][] convertFuzzListToArray(ArrayList<Object[]> list) {
    int len = list.size();
    Object[][] returnArray = new Object[len][3];
    for (int i = 0; i < len; i++) {
      returnArray[i] = list.get(i);
    }

    return returnArray;
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
   * Method to determine if node is mutable by caller or only by the server (i.e. read/write
   * property or read-only). Non-mutable nodes should be ignored and the call accepted if the 
   * rest of the json is valid.
   * 
   * @param nodeDef node to be tested
   * @return boolean
   */
  public static boolean getNodeMutability(JSONObject nodeDef) {
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
  
  public class MetadataEngineInfo{
    public JSONObject modelProperties;
    public JSONObject modelInstance;
    public JSONArray modelInstanceItems;
    public String fuzzPropertyParentPath;
    public String fuzzPropertyKey; 
    public ModelPropertyType fuzzPropertyType;
    public Object fuzzPropertyStartValue;
    public JSONObject fuzzPropertyNodeDefinition;
    public boolean fuzzPropertyIsMutable;
    private String modelSearchPath;
    
    
    public MetadataEngineInfo(String currentPath, String currentKey, ModelPropertyType type, Object currentValue) throws ModelSearchException {
      this.modelProperties = ModelObject.getObjectNodeProperties(entity, entity.getModel());
      if(entity.getModelType() == ModelPropertyType.OBJECT) {
        this.modelInstance = entity.getObjectMetadata();
      }else if(entity.getModelType() == ModelPropertyType.ARRAY) {
        this.modelInstanceItems = entity.getObjectItems();
      }
      this.fuzzPropertyKey = currentKey;
      this.fuzzPropertyParentPath = currentPath;
      this.fuzzPropertyType = type;
      this.fuzzPropertyStartValue = currentValue;
      
      this.modelSearchPath = fuzzPropertyParentPath.concat(fuzzPropertyKey);

      this.fuzzPropertyNodeDefinition = entity.getDefinitionAtModelPath(modelProperties, modelSearchPath);//(JSONObject) modelProperties.get(fuzzPropertyKey);
      
      if(fuzzPropertyNodeDefinition == null) {
        System.out.println("hello");
      }
      
      fuzzPropertyIsMutable = getNodeMutability(fuzzPropertyNodeDefinition);
    }
  }


  /**
   * Fuzzer factory which drives properties to the fuzz engine.
   * 
   * @return Object[]
   */
  @Factory
  public Object[] fuzzProperties()  throws ModelSearchException {
    Object[] result = new Object[propertiesFuzzSet.length];
    //org.json.JSONObject rawSchema = new org.json.JSONObject(entity.getModelString());
    //Schema schema = SchemaLoader.load(rawSchema);
    for (int i = 0; i < propertiesFuzzSet.length; i++) {
      String currentParentPath = propertiesFuzzSet[i][0].toString();
      String currentkey = propertiesFuzzSet[i][1].toString();
      ModelPropertyType type = (ModelPropertyType) propertiesFuzzSet[i][2];
      Object currentValue = entity.getMetadataValue(currentParentPath, currentkey);
      // instance.get(currentkey);
      MetadataEngineInfo fuzzInfo = new MetadataEngineInfo(currentParentPath, currentkey, type, currentValue);

      result[i] = new MetadataEngine(this, fuzzInfo);
    }
    return result;
  }
}
