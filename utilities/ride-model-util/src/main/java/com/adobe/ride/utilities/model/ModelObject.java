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

package com.adobe.ride.utilities.model;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.adobe.ride.utilities.model.exceptions.InvalidSyncReferenceException;
import com.adobe.ride.utilities.model.exceptions.ModelSearchException;
import com.adobe.ride.utilities.model.exceptions.SchemaTypeException;
import com.adobe.ride.utilities.model.exceptions.UnexpectedModelDefinitionException;
import com.adobe.ride.utilities.model.exceptions.UnexpectedModelPropertyTypeException;
import com.adobe.ride.utilities.model.types.ModelPropertyType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.MissingNode;

/**
 * Class useful for loading JSON schema and creating json object instance which adheres to the
 * definitions of the schema.
 * 
 * @author tedcasey
 * 
 */
public class ModelObject {

  Charset encoding = StandardCharsets.UTF_8;

  private static final String default_location = "/schemas/service/objectType.json";

  protected static final Logger logger = Logger.getLogger(ModelObject.class.getName());
  protected final static JSONParser parser = new JSONParser();
  protected final Random randomGen = new Random();
  protected static final ObjectMapper mapper = new ObjectMapper();
  private JsonNode modelPropertiesNodes = null;
  private JSONObject model;
  private ModelPropertyType modelType;
  private JSONObject modelProperties;
  private JSONObject modelDefinitions;
  private JSONArray requiredModelProperties;
  protected boolean requiredOnly = false;
  protected String modelString;
  private final String NULL_MODEL_VALUE = "nulledValue";
  private static final String REFERENCE_KEY = "$ref";
  private String serviceName;
  private String objectPath;
  private String objectName;
  private String objectType;
  protected String resourceLocation;
  protected JSONObject objectMetadata = new JSONObject();
  protected JSONArray objectItems;
  protected JSONObject presetNodes;
  protected Set<String> nodesToBuild;

  public ModelObject() {

  }

  /**
   * Method to load the model from a json string which contains the schema in json-schema compliant
   *
   * @param modelString The string containing the json schemas
   */
  protected void loadModelString(String modelString) {
    try {
      // turn model contents into usable JSONObjects
      model = (JSONObject) parser.parse(modelString);
      modelType = ModelPropertyType.eval(model.get("type").toString());
      if (modelType == ModelPropertyType.OBJECT) {
        modelProperties = (JSONObject) model.get("properties");
        requiredModelProperties = (JSONArray) model.get("required");
        modelDefinitions = (JSONObject) model.get("definitions");
        try {
          modelPropertiesNodes = mapper.readTree(modelProperties.toJSONString());
        } catch (IOException e) {
          logger.log(Level.SEVERE, "An error was thrown while deserializing the JSON content", e);
        }
      } else if (modelType == ModelPropertyType.ARRAY) {
        JSONObject arrayDef = ((JSONObject) model.get("items"));
        modelProperties = ((JSONObject) arrayDef.get("properties"));
        requiredModelProperties = (JSONArray) arrayDef.get("required");
        modelDefinitions = (JSONObject) model.get("definitions");
        try {
          modelPropertiesNodes = mapper.readTree(modelProperties.toJSONString());
        } catch (IOException e) {
          logger.log(Level.SEVERE, "An error was thrown while deserializing the JSON content", e);
        }
      }
    } catch (ParseException e) {
      logger.log(Level.SEVERE, "A Parse exception was thrown", e);
    }
  }

  /**
   * Method to load the model from a file in the resourceLocation which is expected to be
   * json-schema compliant
   *
   * @param resourceLocation the location of the json-schema file in the resources folder. Eg.
   *        /schema/TestService/profile.json
   */
  protected void loadModel(String resourceLocation) {

    // pull in model as String
    try {
      modelString =
          IOUtils.toString(this.getClass().getResourceAsStream(resourceLocation), encoding);
      loadModelString(modelString);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "An IO exception was thrown", e);
    }
  }

  /**
   * Main Model Object Constructor
   * 
   * <a href="https://github.com/adobe/ride/blob/develop/QuickStart.md#java-code">Ride Quickstart:
   * Java Code</a>
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param objectType in the default, among other things, used as a segment of the REST path
   * @param objectName in the default, among other things, used as a segment of the REST path
   * @param useRequiredOnly boolean to determine whether to build only the required json nodes as
   *        defined in the spec
   */
  public ModelObject(String serviceName, String objectType, String objectName,
      boolean useRequiredOnly) {
    initializeModelObject(serviceName, objectType, objectName, null, null, useRequiredOnly);
  }

  /**
   * Model Object Constructor which allows user to start with preset nodes that are not overwritten
   * on data generation
   * 
   * <a href="https://github.com/adobe/ride/blob/develop/QuickStart.md#java-code">Ride Quickstart:
   * Java Code</a>
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param objectType in the default, among other things, used as a segment of the REST path
   * @param objectName in the default, among other things, used as a segment of the REST path
   * @param presetNodes set of JSON nodes and values to be used in the object, not generated
   *        dynamically
   * @param useRequiredOnly boolean to determine whether to build only the required json nodes as
   *        defined in the spec
   */
  public ModelObject(String serviceName, String objectType, String objectName,
      JSONObject presetNodes, boolean useRequiredOnly) {
    this.presetNodes = presetNodes;
    initializeModelObject(serviceName, objectType, objectName, presetNodes, null, useRequiredOnly);
  }

  /**
   * Model Object Constructor which allows user to start with preset nodes that are not overwritten
   * on data generation and also allows the user to specify nodes to be built with an array of
   * standard slash delimited paths (i.e. /this/that/theothernode)
   * 
   * <a href="https://github.com/adobe/ride/blob/develop/QuickStart.md#java-code">Ride Quickstart:
   * Java Code</a>
   * 
   * @param serviceName name of the service, which is a mapping to the config folder in the project
   *        resources
   * @param objectType in the default, among other things, used as a segment of the REST path
   * @param objectName in the default, among other things, used as a segment of the REST path
   * @param presetNodes set of JSON nodes and values to be used in the object, not generated
   *        dynamically
   * @param nodesToBuild set of JSONnodes which are to be dynamically generated. Remaining schema
   *        defined nodes will not be generated
   */
  public ModelObject(String serviceName, String objectType, String objectName,
      JSONObject presetNodes, Set<String> nodesToBuild) {
    this.presetNodes = presetNodes;
    initializeModelObject(serviceName, objectType, objectName, presetNodes, nodesToBuild, true);
  }

  private void initializeModelObject(String serviceName, String objectType, String objectName,
      JSONObject presetNodes, Set<String> nodesToBuild, boolean useRequiredOnly) {

    if (presetNodes != null) {
      this.presetNodes = presetNodes;
    }

    if (nodesToBuild != null) {
      this.nodesToBuild = nodesToBuild;
    }

    resourceLocation = default_location.replace("service", serviceName.toString())
        .replace("objectType", objectType);
    this.serviceName = serviceName;
    this.objectType = objectType;
    this.setObjectName(objectName);
    this.setObjectPath(objectType + "/" + objectName);
    this.setRequiredOnly(useRequiredOnly);
    loadModel(resourceLocation);
  }

  /**
   * Create a model object from a json schema from a specified resource location
   * 
   * @param resourceLocation location of the schema in the project resources
   */
  public ModelObject(String resourceLocation) {
    initializeModelObject(resourceLocation, null, null, false);
  }

  /**
   * Create a model object from a json schema from a specified resource location and use a
   * predefined set of nodes to start with.
   * 
   * @param resourceLocation location of the schema within the project resources.
   * @param presetNodes set of JSON nodes and values to be used in the object, not generated
   *        dynamically
   * @param useRequiredOnly boolean to determine whether to build only the required json nodes as
   *        defined in the spec
   */
  public ModelObject(String resourceLocation, JSONObject presetNodes, boolean useRequiredOnly) {
    initializeModelObject(resourceLocation, presetNodes, null, false);
  }

  /**
   * Create a model object from a json schema from a specified resource location and use a
   * predefined set of nodes to start with. Also only build a targeted set of nodes.
   * 
   * @param resourceLocation location of the schema within the project resources.
   * @param presetNodes set of JSON nodes and values to be used in the object, not generated
   *        dynamically
   * @param nodesToBuild set of JSONnodes which are to be dynamically generated. Remaining schema
   *        defined nodes will not be generated.
   * @param useRequiredOnly boolean to determine whether to build only the required json nodes as
   *        defined in the spec
   */
  public ModelObject(String resourceLocation, JSONObject presetNodes, Set<String> nodesToBuild,
      boolean useRequiredOnly) {
    initializeModelObject(resourceLocation, presetNodes, nodesToBuild, useRequiredOnly);
  }

  private void initializeModelObject(String resourceLocation, JSONObject presetNodes,
      Set<String> nodesToBuild, boolean useRequiredOnly) {
    if (presetNodes != null) {
      this.presetNodes = presetNodes;
    }

    if (nodesToBuild != null) {
      this.nodesToBuild = nodesToBuild;
    }

    this.requiredOnly = useRequiredOnly;

    this.resourceLocation = resourceLocation;
    loadModel(resourceLocation);
  }

  /**
   * Constructor to initialize with a JSON schema string
   * 
   * @param modelString string which conforms to JSON schema standards
   * @param useRequiredOnly boolean to determine whether to build only the required json nodes as
   *        defined in the spec
   */
  public ModelObject(String modelString, boolean useRequiredOnly) {
    this.setRequiredOnly(useRequiredOnly);
    this.modelString = modelString;
    loadModelString(modelString);
  }


  /**
   * 
   * @return JSONObject representation of the model identified in the constructor
   */
  public JSONObject getModel() {
    return model;
  }

  /**
   * 
   * @return JSONObject representation of the properties of the model
   */
  public JSONObject getModelProperties() {
    return modelProperties;
  }

  /**
   * 
   * @return JSONArray representation of the required properties of the model
   */
  public JSONArray getRequiredModelProperties() {
    return requiredModelProperties;
  }

  /**
   * 
   * @return boolean value of whether or not to initialize only required properties of the model
   *         when building a valid model instance.
   */
  public boolean getRequiredOnlyProperties() {
    return requiredOnly;
  }

  /**
   * 
   * @param useRequiredOnly boolean to set whether to use required only properties when building a
   *        valid model instance. This is a hierarchical setting, meaning nodes which are required
   *        nodes of the model, if defined by a definition, will only have their required values
   *        created.
   */
  public void setRequiredOnly(boolean useRequiredOnly) {
    requiredOnly = useRequiredOnly;
  }

  /**
   * 
   * @return value of whether the object
   */
  public boolean getRequiredOnly() {
    return requiredOnly;
  }

  /**
   * 
   * @return string representation of the model indicated in the constructor.
   */
  public String getModelString() {
    return modelString;
  }

  protected String getModelNullConst() {
    return NULL_MODEL_VALUE;
  }

  /**
   * Method to retrieve the dictionary associated to the a schema node definition
   * 
   * @param propertyObject schema node which contains a definition reference
   * @return definition associated with the passed schema node
   * @throws UnexpectedModelPropertyTypeException thrown when one or more of the ModelPropertyTypes
   *         of the nodes passed in the JSONObject cannot be determined
   */
  protected JSONObject getDefinitionRef(JSONObject propertyObject)
      throws UnexpectedModelPropertyTypeException {
    return (JSONObject) modelDefinitions.get(getDefinitionName(propertyObject));
  }

  /**
   * Method which returns the name of the library to which a node definition is associated.
   * 
   * @param propertyObject JSONObject node to be analyzed
   * @return String name of the library definition associated with the node
   * @throws UnexpectedModelPropertyTypeException thrown when one or more of the ModelPropertyTypes
   *         of the nodes passed in the JSONObject cannot be determined
   */
  protected String getDefinitionName(JSONObject propertyObject)
      throws UnexpectedModelPropertyTypeException {
    String defname;
    if (getModelPropertyType(propertyObject) != ModelPropertyType.REF_DEFINITION) {
      throw new UnexpectedModelPropertyTypeException(propertyObject);
    } else {
      String definition_string = propertyObject.get(REFERENCE_KEY).toString();
      String[] definition = definition_string.split("/");
      defname = definition[definition.length - 1];
    }
    return defname;
  }

  /**
   * Method which determines and returns the ModelPropertyTypes that the json schema definition maps
   * to. If it encounters a type that is not enumerated, it throws the
   * {@link UnexpectedModelPropertyTypeException} Exception.
   * 
   * @param object JSONObject node in the model from which the type is to be determined
   * @return ModelPropertyType One of the enumerated ModelPropertyTypes
   * @throws UnexpectedModelPropertyTypeException thrown when one or more of the ModelPropertyTypes
   *         of the nodes passed in the JSONObject cannot be determined
   */
  public static ModelPropertyType getModelPropertyType(JSONObject object)
      throws UnexpectedModelPropertyTypeException {

    if (object == null) {
      throw new NullPointerException(
          "A null object was passed to the ModelObject.getModelPropertyType method.");
    }
    String type = (object.containsKey("type")) ? object.get("type").toString() : "noType";

    ModelPropertyType returnValue = null;
    if (object.containsKey("sync")) {
      returnValue = ModelPropertyType.SYNC;
    } else if (object.containsKey("anyOf")) {
      returnValue = ModelPropertyType.ANYOF;
    } else if (object.containsKey("enum")) {
      returnValue = ModelPropertyType.ENUM;
    } else if (type.equals("array")) {
      returnValue = ModelPropertyType.ARRAY;
    } else if (object.containsKey(REFERENCE_KEY)) {
      if (object.get(REFERENCE_KEY).toString().startsWith("#")) {
        returnValue = ModelPropertyType.REF_DEFINITION;
      } else {
        returnValue = ModelPropertyType.REF_SCHEMA;
      }
    } else if (object.containsKey("format")) {
      String format = object.get("format").toString();
      if (format.equals("date")) {
        returnValue = ModelPropertyType.DATE;
      } else if (format.equals("date-time")) {
        returnValue = ModelPropertyType.DATETIME;
      } else if (format.equals("timestamp")) {
        returnValue = ModelPropertyType.TIMESTAMP;
      } else if (format.equals("email")) {
        returnValue = ModelPropertyType.EMAIL;
      } else if (format.equals("ipv6")) {
        returnValue = ModelPropertyType.IPV6;
      } else if (format.equals("ipv4")) {
        returnValue = ModelPropertyType.IPV4;
      } else if (format.equals("uri")) {
        returnValue = ModelPropertyType.URI;
      } else if (format.equals("uri-reference")) {
        returnValue = ModelPropertyType.URI_REF;
      }
    } else if (object.containsKey("pattern")) {
      returnValue = ModelPropertyType.PATTERN;
    }

    if (returnValue == null && !"noType".equals(type)) {
      returnValue = ModelPropertyType.eval(type);
    }

    if (returnValue == null) {
      throw new UnexpectedModelPropertyTypeException(object);
    }

    return returnValue;
  }

  /**
   * Method to retrieve one of the definitions from the anyOf value from a schema node definition
   * 
   * @param propertyObject the node from a schema which has a type of 'AnyOf'
   * @return Object random member definition of the one of the objects in the AnyOf json schema
   *         field.
   */
  private Object getOneOfAnyOf(JSONObject propertyObject) {
    Object[] array = ((JSONArray) propertyObject.get("anyOf")).toArray();
    Validate.isTrue(array.length > 0);
    JSONObject nodeDef = (JSONObject) array[DataGenerator.generateRandomInt(0, array.length - 1)];
    Object object = null;;
    try {
      object = generateNodeValue(null, null, nodeDef);
    } catch (ModelSearchException e) {
      e.printStackTrace();
    }
    return object;
  }

  /**
   * Method which returns the pattern definition of a schema node which purports to have one. If the
   * model doesn't have that property, or that property doesn't not have a 'pattern' key, it throws
   * a {@link ModelSearchException}.
   * 
   * @param property model node from which the RegEx pattern specification is to be returned
   * @return String
   * @throws ModelSearchException Exception thrown when function cannot find the property associated
   *         with the property parameter
   */
  public String getPropertyPattern(String property) throws ModelSearchException {
    if (!modelProperties.containsKey(property)) {
      throw new ModelSearchException(modelProperties, property);
    } else if (!(((JSONObject) modelProperties.get(property)).containsKey("pattern"))) {
      throw new ModelSearchException((JSONObject) modelProperties.get(property), "pattern");
    } else {
      return (((JSONObject) modelProperties.get(property)).get("pattern")).toString();
    }
  }

  /**
   * Builds a JSON Object instance which conforms to the specifications of the model and other
   * settings of this class such as useRequiredOnly.
   * 
   * @return Object JSON Object of model type
   */
  public Object buildValidModelInstance() {
    if (modelType == ModelPropertyType.OBJECT) {
      if (presetNodes != null && objectMetadata.isEmpty()) {
        objectMetadata = presetNodes;
      }

      if (nodesToBuild != null) {
        return buildTargetedNodes();
      } else {
        JSONObject modelSet = (requiredOnly) ? getRequiredOnlyDefs(model) : modelProperties;
        return buildModelInstance(modelSet);
      }
    } else if (modelType == ModelPropertyType.ARRAY) {
      try {
        ArrayNode generatedItems = buildArrayNode(model);
        String itemsString = generatedItems.toString();
        objectItems = (JSONArray) (parser.parse(itemsString));
      } catch (ParseException e) {
        logger.log(Level.SEVERE, e.getMessage());
      }
      return objectItems;
    } else {
      // Handling Primitive type schemas
      return generateNodeValue(model);
    }
  }

  private Object buildTargetedNodes() {
    for (String str : nodesToBuild) {
      buildNodeValueAtPath(str);
    }
    return objectMetadata;
  }

  /**
   * Builds a NEW json Object instance which conforms to the specifications of the model and other
   * settings of this class such as useRequiredOnly. This can be used when you don't want to parse
   * the schema again and yet want a completely new Object.
   *
   * @return Object JSON Object of model type
   */
  public Object buildNewModelInstance() {
    if (presetNodes != null) {
      objectMetadata = new JSONObject();
    } else {
      objectMetadata = presetNodes;
    }
    return buildValidModelInstance();
  }

  /**
   * Generate x number of valid instances of the defined object.
   * 
   * @param numberOfInstances Number of instances to be generated from the model (schema)
   * @return JSONArray
   */
  @SuppressWarnings("unchecked")
  public JSONArray generateModelInstances(int numberOfInstances) {

    if (presetNodes != null) {
      objectMetadata = new JSONObject();
    } else {
      objectMetadata = presetNodes;
    }

    JSONArray returnArray = new JSONArray();
    for (int i = 0; i < numberOfInstances; i++) {
      returnArray.add((JSONObject) buildNewModelInstance());
    }
    return returnArray;

  }

  /**
   * Method to return the model with only the required properties and their definitions
   * 
   * @param objModel model from which the required only properties are to be
   * @return JSONObject model representation with only the required properties and their definitions
   */
  @SuppressWarnings("unchecked")
  private JSONObject getRequiredOnlyDefs(JSONObject objModel) {
    JSONObject returnObjProps = new JSONObject();
    JSONObject allProps = (JSONObject) objModel.get("properties");
    JSONArray reqProps = (JSONArray) objModel.get("required");
    if (reqProps != null) {
      Iterator<String> i = reqProps.iterator();
      Set<String> keyset = allProps.keySet();
      while (i.hasNext()) {
        String req_property = i.next().toString();
        for (String s : keyset) {
          if (req_property.equals(s.toString())) {
            returnObjProps.put(s, allProps.get(s));
          }
        }
      }
    }
    return returnObjProps;
  }

  /**
   * Method which builds a JSON instance which conforms to a given schema.
   * 
   * @param modelObject source from which to build the instance
   * @return JSONObject instance which conforms to the given model
   */
  @SuppressWarnings("unchecked")
  private JSONObject buildModelInstance(JSONObject modelObjectProps) {

    Set<Entry<String, JSONObject>> set = modelObjectProps.entrySet();
    for (Entry<String, JSONObject> e : set) {
      String key = e.getKey();
      if (!key.equals("$schema")) {
        JSONObject obj = e.getValue();
        // generated node value and add to root.
        try {
          generateNodeValue("/", key, obj);
        } catch (ModelSearchException e1) {
          e1.printStackTrace();
        }
      }
    }

    return objectMetadata;
  }

  /**
   * Convenience method to print a human readable representation of the passed JSONObject for
   * debugging purposes.
   * 
   * @param object json object to be printed to the console in a human readable format.
   */
  public static void prettyPrintToConsole(Object object) {
    if (object == null) {
      throw new NullPointerException(
          "A null object was passed to the ModelObject.prettyPrintToConsole method.");
    }
    try {

      logger.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object));
    } catch (JsonProcessingException e) {
      logger.log(Level.SEVERE, "An JSON processing exception was encountered", e);
    }
  }

  /**
   * Method which builds a JSON object which conforms to a JSON schema definition passed in the
   * argument.
   * 
   * @param pathToParent standard full path to the parent of the object in the schema
   * @param obj schema definition
   * @return JSONObject instance which conforms to the definition passed
   */
  @SuppressWarnings("unchecked")
  protected JSONObject buildDefinedObjectNode(String pathToParent, JSONObject obj) {
    JSONObject returnObj = new JSONObject();
    try {
      String key = "";
      JSONObject definition = getDefinitionRef(obj);
      if (definition.containsKey("patternProperties")) {
        JSONObject patternProps = (JSONObject) definition.get("patternProperties");

        Set<Entry<String, JSONObject>> set = patternProps.entrySet();

        for (Entry<String, JSONObject> e : set) {
          key = DataGenerator.generateRegexValue(e.getKey());
          returnObj.put(key, generateNodeValue(null, key, e.getValue()));
        }
      } else if (definition.containsKey("properties")) {
        JSONObject props = (requiredOnly) ? getRequiredOnlyDefs(definition)
            : (JSONObject) definition.get("properties");

        Set<Entry<String, JSONObject>> set = props.entrySet();

        for (Entry<String, JSONObject> e : set) {
          key = e.getKey();
          Object existingValue = checkForExisitingValue(pathToParent, key);
          if (existingValue == null) {
            JSONObject propertyDef = e.getValue();
            returnObj.put(key, generateNodeValue(null, key, propertyDef));
          } else {
            returnObj.put(key, existingValue);
          }
        }
      } else if (definition.containsKey("items")) {
        ArrayNode arrayObj = buildArrayNode(definition);
        returnObj.put(key, arrayObj);
      } else {
        try {
          throw new UnexpectedModelDefinitionException(definition);
        } catch (UnexpectedModelDefinitionException e) {
          logger.log(Level.SEVERE, e.getMessage());
        }
        return null;
      }
    } catch (UnexpectedModelPropertyTypeException e) {
      logger.log(Level.SEVERE, "An Unexpected Model type was encountered", e);
    } catch (ModelSearchException mse) {
      mse.printStackTrace();
    }
    return returnObj;
  }

  /**
   * Method which builds nodes of schema type Object.
   * 
   * @param pathToParent standard full path to the parent of the object in the schema
   * @param obj node the definition from which the JSON Object is to be built
   * @return JSONObject object built from the node definition
   * 
   */
  @SuppressWarnings("unchecked")
  private JSONObject buildObjectNode(String pathToParent, JSONObject obj) {
    JSONObject returnObj = new JSONObject();
    if (obj.containsKey("patternProperties")) {
      JSONObject props = (JSONObject) obj.get("patternProperties");
      Object[] set = props.keySet().toArray();
      Object customSet = buildCustomObject(set[0].toString(), 3);
      returnObj = (JSONObject) customSet;
    } else if (obj.containsKey("properties")) {
      JSONObject props =
          (requiredOnly) ? getRequiredOnlyDefs(obj) : (JSONObject) obj.get("properties");
      Set<Entry<String, JSONObject>> set = props.entrySet();

      for (Entry<String, JSONObject> e : set) {
        JSONObject propertyDef = e.getValue();
        String currentkey = e.getKey();
        Object existingValue = checkForExisitingValue(pathToParent, currentkey);
        
        if (existingValue == null) {
          Object genValue = null;
          try {
            genValue = generateNodeValue(null, currentkey, propertyDef);
          } catch (ModelSearchException e1) {
            e1.printStackTrace();
          }
          returnObj.put(currentkey, genValue);
        } else {
          try {
            ModelPropertyType type = getModelPropertyType(propertyDef);
            if(type == ModelPropertyType.OBJECT){
              String subPath = pathToParent+"/"+currentkey;
              System.out.println("Subpath: "+subPath);
              Object newValue = buildObjectNode(subPath, propertyDef);
              returnObj.put(currentkey, newValue);
            }else {
              returnObj.put(currentkey, existingValue);
            }
          } catch (UnexpectedModelPropertyTypeException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
        }
      }
    } else {
      // no properties, so, build random generic object
      returnObj.put(DataGenerator.generateRegexValue(DataGenerator.genericRegex),
          DataGenerator.generateRegexValue(DataGenerator.genericRegex));
    }

    return returnObj;
  }

  /**
   * Method to see if a value exists at a path before attempting to generate a new value.
   * 
   * @param parentPath standard full path to the parent of the object in the schema
   * @param key name of the node
   * @return Object
   */
  private Object checkForExisitingValue(String parentPath, String key) {
    Object exisitingValue = null;
    if (parentPath == null || parentPath == null + "/" + null) {
      exisitingValue = null;
    } else {
      try {
        JsonNode tree = mapper.readTree(objectMetadata.toJSONString());
        JsonNode value = ((ObjectNode) tree.at(parentPath)).get(key);
        if (value != null && !value.isMissingNode()) {
          exisitingValue = value;
        } else {
          exisitingValue = null;
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, e.getMessage());
      }
    }
    return exisitingValue;
  }

  /**
   * 
   * @param keyPattern string value specified in the patternProperties of the custom object
   *        definition
   * @param numProps number of properties to create in the custom object
   * @return Object
   */
  @SuppressWarnings("unchecked")
  private Object buildCustomObject(String keyPattern, int numProps) {
    JSONObject returnObj = new JSONObject();
    for (int i = 0; i < numProps; i++) {
      JSONObject indObj = new JSONObject();
      indObj.put("type", "object");
      String key = DataGenerator.generateRegexValue(keyPattern);
      Object value = buildObjectNode(null, indObj);
      returnObj.put(key, value);
    }

    return returnObj;
  }

  @SuppressWarnings("unchecked")
  private ArrayNode buildArrayNode(JSONObject propertyDef) {
    // TODO: Improve handling for min and max values
    ArrayList<Object> buildArray = new JSONArray();
    JSONObject itemProps = (JSONObject) propertyDef.get("items");
    int arrayLength;
    if (propertyDef.containsKey("maxItems")
        && (Integer.parseInt(propertyDef.get("maxItems").toString()) > 0)) {
      int max = Integer.parseInt(propertyDef.get("maxItems").toString());
      arrayLength = max;
    } else {
      arrayLength = 3;
    }

    for (int i = 0; i < arrayLength; i++) {
      Object item = null;
      try {
        item = generateNodeValue(null, null, itemProps);
      } catch (ModelSearchException e) {
        e.printStackTrace();
      }
      buildArray.add(item);
    }

    return mapper.valueToTree(buildArray);
  }

  /**
   * Method to create an object of type "sync" which relies on the values of other nodes within the
   * instance.
   * 
   * @param propertyDef JSON definition of the node to be built
   * @return String
   * @throws InvalidSyncReferenceException
   */
  private String createSyncdValue(JSONObject propertyDef) throws InvalidSyncReferenceException {
    String pattern = propertyDef.get("sync").toString();
    String startStr = "{$path:";
    int startSearchStringLength = startStr.length();
    String endStr = "}";
    int endSearchStringLength = endStr.length();
    int endStringIndex = 0;
    String returnValue = "";

    while (endStringIndex != -1) {
      // find start index of path reference
      int startStringIndex = pattern.indexOf(startStr, endStringIndex);

      // check to see if there is path ref beyond the last check
      if (startStringIndex != -1) {

        if (startStringIndex != 0 && endStringIndex != 0) {
          endStringIndex += endSearchStringLength;
        }

        // generate value between last sync value and this
        String nonSyncVal =
            DataGenerator.generateRegexValue(pattern.substring(endStringIndex, startStringIndex));
        returnValue += nonSyncVal;

        // find end index of path reference
        endStringIndex = pattern.indexOf(endStr, startStringIndex);

        if (endStringIndex != -1) {
          String propertyPath =
              pattern.substring(startStringIndex + startSearchStringLength, endStringIndex);

          String syncVal = getMappedValue(propertyPath, true);
          returnValue += syncVal;
        } else {
          throw new InvalidSyncReferenceException(model, pattern);
        }
      } else {
        String remainder = DataGenerator
            .generateRegexValue(pattern.substring(endStringIndex + 1, pattern.length())).toString();
        returnValue += remainder;
        endStringIndex = -1;
      }
    }
    // Compensate for JsonNode escaping internal quotes in json object.
    return returnValue.replace("\"", "");
  }

  /**
   * Method to get the value at a specific property path.
   * 
   * @param propertyPath full standard path to the property in the json object
   * @param completeBranch boolean in indicate whether to build the rest of the branch if value not
   *        present
   * @return
   */
  private String getMappedValue(String propertyPath, boolean completeBranch) {

    JsonNode node = null;
    try {
      node = mapper.readTree(objectMetadata.toJSONString());
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getMessage());
      e.printStackTrace();
    }

    JsonNode value = node.at(propertyPath);

    if (value.isMissingNode() || value == null) {
      if (completeBranch) {
        return completeBranchAndReturnValue(propertyPath).toString();
      } else {
        return null;
      }
    } else {
      return value.toString();
    }
  }

  /**
   * Re-synchronize object after change in data
   * 
   */
  public void syncObject() {
    buildValidModelInstance();
  }

  /**
   * Method to complete branch when it's path is among the target node set
   * 
   * @param propertyPath path to the property in the JSON object
   * @return Object
   */
  private Object buildNodeValueAtPath(String propertyPath) {
    Object returnValue = null;
    JsonNode objectJsonNodeData = null;
    try {
      objectJsonNodeData = mapper.readTree(objectMetadata.toJSONString());
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getMessage());
    }

    String[] pathParts = propertyPath.split("/");
    int searchableTermsLength = pathParts.length;
    String pathSearchString = "";
    String parentSearchString = "";
    String nodeName = "";
    JsonNode nodeModelDef = null;
    for (int i = 1; i < searchableTermsLength; i++) {
      nodeName = pathParts[i];
      if (i == 1) {
        parentSearchString = "/";
        nodeModelDef = modelPropertiesNodes.at(parentSearchString + nodeName);
      } else {
        parentSearchString = pathSearchString;
        nodeModelDef = nodeModelDef.at("/properties").get(nodeName);
      }

      pathSearchString += "/" + nodeName;

      if (i != searchableTermsLength - 1
          && objectJsonNodeData.at(pathSearchString).isMissingNode()) {
        if (parentSearchString.equals("/")) {
          ((ObjectNode) (objectJsonNodeData)).putObject(nodeName);
        } else {
          ((ObjectNode) objectJsonNodeData.at(parentSearchString)).putObject(nodeName);
        }
      }
    }

    try {
      objectMetadata = (JSONObject) parser.parse(objectJsonNodeData.toString());
    } catch (ParseException e1) {
      e1.printStackTrace();
    }

    ObjectNode currentValue =
        (ObjectNode) (objectJsonNodeData.at(parentSearchString).get(nodeName));
    if (currentValue == null || currentValue.isMissingNode() || currentValue.isEmpty(null)
        || currentValue.isNull()) {
      JSONObject nodeDef = null;
      if (parentSearchString != "/") {
        try {
          String stringRepOfMappedNode = mapper.writeValueAsString(nodeModelDef);
          nodeDef = ((JSONObject) parser.parse(stringRepOfMappedNode));
        } catch (JsonProcessingException e) {
          logger.log(Level.SEVERE, e.getMessage());
        } catch (ParseException e) {
          logger.log(Level.SEVERE, e.getMessage());
        }
      } else {
        nodeDef = (JSONObject) modelProperties.get(nodeName);
      }

      try {
        returnValue = generateNodeValue(parentSearchString, nodeName, nodeDef);
      } catch (ModelSearchException e) {
        e.printStackTrace();
      }
    } else {
      returnValue = currentValue;
    }

    return returnValue;

  }

  /**
   * Method to complete a branch that is referenced by another while data is being generated
   * 
   * @param propertyPath path to the property in the JSON object
   * @return Object
   */
  private Object completeBranchAndReturnValue(String propertyPath) {
    Object returnValue = null;

    String[] pathParts = propertyPath.split("/");
    int searchableTermsLength = pathParts.length - 1;

    String pathSearchString = "";
    String parentSearchString = "";
    String nodeName = "";
    JsonNode objectJsonNodeData = null;
    try {
      objectJsonNodeData = mapper.readTree(objectMetadata.toJSONString());
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getMessage());
    }

    for (int i = 1; i <= searchableTermsLength; i++) {
      if (i > 1) {
        parentSearchString = pathSearchString;
        pathSearchString += "/" + pathParts[i];
      } else {
        parentSearchString = "/";
        pathSearchString += "/" + pathParts[i];
      }

      JsonNode currentValue = objectJsonNodeData.at(pathSearchString);
      if (currentValue == null || objectJsonNodeData.at(pathSearchString).isMissingNode()
          || currentValue.isNull()) {
        JSONObject nodeDef = null;
        nodeName = pathParts[i];
        if (parentSearchString != "/") {
          JsonNode modelDef = modelPropertiesNodes.at(parentSearchString);
          JsonNode nodeModelDef = modelDef.at("/properties").get(nodeName);
          try {
            String stringRepOfMappedNode = mapper.writeValueAsString(nodeModelDef);
            nodeDef = ((JSONObject) parser.parse(stringRepOfMappedNode));
          } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, e.getMessage());
          } catch (ParseException e) {
            logger.log(Level.SEVERE, e.getMessage());
          }

        } else {
          nodeDef = (JSONObject) modelProperties.get(nodeName);
        }

        try {
          generateNodeValue(parentSearchString, nodeName, nodeDef);
        } catch (ModelSearchException e) {
          e.printStackTrace();
        }
      }
    }
    JsonNode updatedTree = null;
    try {
      updatedTree = mapper.readTree(objectMetadata.toJSONString());
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getMessage());
    }
    ObjectNode objectmine = null;
    if (parentSearchString != "/") {
      objectmine = (ObjectNode) updatedTree.at(parentSearchString);
    } else {
      objectmine = (ObjectNode) updatedTree;
    }
    returnValue = objectmine.get(nodeName);
    return returnValue;
  }

  /* TODO: Leave this in. May use it later */
  @SuppressWarnings("unused")
  private static Object castValueAsModelType(ModelPropertyType type, String value) {
    try {
      switch (type) {
        case ARRAY:
          return parser.parse(value);
        case BOOLEAN:
          return Boolean.parseBoolean(value);
        case DATETIME:
          return DateTime.parse(value);
        case INTEGER:
          return Integer.valueOf(value);
        case NULL:
          return null;
        case NUMBER:
          return Float.parseFloat(value);
        case OBJECT:
          return parser.parse(value);
        case STRING:
          return value;
        case URI:
          return URI.create(value);
        case URI_REF:
          return URI.create(value);
        case BYTE:
          return Byte.parseByte(value);
        default:
          return "Unknown Cast Type";
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage());
      return null;
    }
  }

  /**
   * Alternate signature for generateNodeValue if there is no intent to assign the value anywhere in
   * the objectMetadata
   * 
   * @param propertyDef model definition to use when building value for a node
   * @return Object
   */
  public Object generateNodeValue(JSONObject propertyDef) {
    try {
      return generateNodeValue(null, null, propertyDef);
    } catch (ModelSearchException e) {
      logger.log(Level.SEVERE, e.getMessage());
    }
    return null;
  }

  /**
   * Method which generates a value which conforms to the node definition passed in the argument. If
   * this method encounters an unexpected ModelPropertyType or ModelDefintion, it throws a
   * {@link UnexpectedModelPropertyTypeException} or {@link UnexpectedModelDefinitionException}
   * respectively. If a path to be set is passed in, this method will not overwrite an existing
   * value at that path, should it find one.
   * 
   * @param parentPath path to the parent node of the node to be set
   * @param key name of the node.
   * @param propertyDef model definition to use when building value for a node
   * @return Object
   * @throws ModelSearchException thrown when the parent path cannot be found in the model
   */
  public Object generateNodeValue(String parentPath, String key, JSONObject propertyDef)
      throws ModelSearchException {
    
    String nodePath = "";
    Object returnValue = null;
    JsonNode dataTree = null;
    try {
      dataTree = mapper.readTree(objectMetadata.toJSONString());
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getMessage());
    }

    if (parentPath == null) {
      nodePath = null;
    } else if (parentPath != "/") {
      nodePath += parentPath + "/" + key;
    } else {
      nodePath = "/" + key;
    }

    ModelPropertyType type = ModelPropertyType.NULL;

    try {
      if (propertyDef == null) {
        throw new ModelSearchException(this.model, key);
      }
      type = getModelPropertyType(propertyDef);
    } catch (UnexpectedModelPropertyTypeException e) {
      logger.log(Level.SEVERE, "An Unexpected Model Type was encountered", e);
    }

    Object exisitingValue = null;
    if (parentPath != null) {// if we're just generating a value and returning it, we don't check
                             // if the node exists
      exisitingValue = dataTree.at(nodePath);
    }

    JsonNode existingParentValue = dataTree.at(parentPath);

    if (exisitingValue != null && !dataTree.at(nodePath).isMissingNode() && type != ModelPropertyType.OBJECT && type != ModelPropertyType.REF_DEFINITION && type != ModelPropertyType.REF_SCHEMA) {

      return dataTree.at(parentPath).get(key);

    } else {
      // create empty node, if not working with Root Array
      if (parentPath != null && !existingParentValue.isNull()) {
        if (parentPath == "/" && ((dataTree.at(nodePath).isMissingNode()) || (dataTree.at(nodePath) == null))) {
          ((ObjectNode) dataTree).putObject(key);
        } else if((dataTree.at(nodePath).isMissingNode()) || (dataTree.at(nodePath) == null)){
          ((ObjectNode) dataTree.at(parentPath)).putObject(key);
        }
        refreshMetadata(dataTree);
      }

      boolean exclusiveMin = (propertyDef.containsKey("exclusiveMinimum"))
          ? Boolean.parseBoolean(propertyDef.get("exclusiveMinimum").toString())
          : false;
      boolean exclusiveMax = (propertyDef.containsKey("exclusiveMaximum"))
          ? Boolean.parseBoolean(propertyDef.get("exclusiveMaximum").toString())
          : false;

      switch (type) {
        case SYNC:
          try {
            returnValue = createSyncdValue(propertyDef);
          } catch (InvalidSyncReferenceException e) {
            logger.log(Level.SEVERE, e.getMessage());
          }
          break;
        case PATTERN:
          String patternDef = propertyDef.get("pattern").toString();
          // hack to get around generex limitations
          String value;
          if (patternDef.contains("?!")) {
            String[] productIdPattern = patternDef.split("\\)");
            value = DataGenerator.generateRegexValue(productIdPattern[1]);
          } else {
            value = DataGenerator.generateRegexValue(patternDef);
          }
          returnValue = value;
          break;
        case ANYOF:
          Object oneObj = getOneOfAnyOf(propertyDef);
          if (oneObj instanceof JSONObject) {
            type = ModelPropertyType.OBJECT;
          } else if (oneObj instanceof Double) {
            type = ModelPropertyType.NUMBER;
          } else {
            type = ModelPropertyType.STRING;
          }
          returnValue = oneObj;
          break;
        case ARRAY:
          ArrayNode array = buildArrayNode(propertyDef);
          returnValue = array;
          break;
        case BOOLEAN:
          boolean bool = DataGenerator.generateRandomBoolean();
          returnValue = bool;
          break;
        case DATE:
          String date = DataGenerator.generateRandomPassedDate(5000, 8000);
          returnValue = date;
          break;
        case DATETIME:
          String datetime = DataGenerator.generateStdDateTime(0);
          returnValue = datetime;
          break;
        case TIMESTAMP:
          String timestamp = DataGenerator.generateTimestamp();
          returnValue = timestamp;
          break;
        case EMAIL:
          String email = DataGenerator.generateEmail();
          returnValue = email;
          break;
        case IPV4:
          String ipv4 = DataGenerator.generateIPv4();
          returnValue = ipv4;
          break;
        case IPV6:
          String ipv6 = DataGenerator.generateIPv6();
          returnValue = ipv6;
          break;
        case REF_DEFINITION:
          JSONObject obj = buildDefinedObjectNode(nodePath, propertyDef);
          returnValue = obj;
          break;
        case REF_SCHEMA:
          ModelObject newObj;
          if (serviceName != null) {
            String objectString = propertyDef.get(REFERENCE_KEY).toString().replace(".json", "");
            String objectType = objectString.split("/")[objectString.split("/").length - 1];
            newObj = new ModelObject(serviceName, objectType, objectName, requiredOnly);
          } else {
            String objectString = propertyDef.get(REFERENCE_KEY).toString();
            String relativeSchema;
            relativeSchema = getRelativeResourceLocation(resourceLocation, objectString);
            newObj = new ModelObject(relativeSchema);
          }
          returnValue = newObj.buildValidModelInstance();
          break;
        case ENUM:
          JSONArray enums = new JSONArray();
          try {
            enums = (JSONArray) parser.parse(propertyDef.get("enum").toString());
          } catch (ParseException e) {
            logger.log(Level.SEVERE, "A Parse exception was thrown", e);
          }
          Object[] enumArray = enums.toArray();
          String enumValue =
              enumArray[DataGenerator.generateRandomInt(0, enumArray.length - 1)].toString();
          returnValue = enumValue;
          break;
        case INTEGER:
          long min = 0;
          long max = 10;
          if (propertyDef.containsKey("minimum")) {
            min = Long.parseLong(propertyDef.get("minimum").toString());
          }
          if (propertyDef.containsKey("maximum")) {
            max = Long.parseLong(propertyDef.get("maximum").toString());
          }
          long val = DataGenerator.generateRandomNumber(min, max, exclusiveMin, exclusiveMax);
          returnValue = val;
          break;
        case NULL:
          returnValue = null;
          break;
        case NUMBER:
          double minNum = 0;
          double maxNum = 10;
          if (propertyDef.containsKey("minimum")) {
            minNum = Double.parseDouble(propertyDef.get("minimum").toString());
          }
          if (propertyDef.containsKey("maximum")) {
            maxNum = Double.parseDouble(propertyDef.get("maximum").toString());
          }
          Number num =
              DataGenerator.generateRandomNumber(minNum, maxNum, exclusiveMin, exclusiveMax);
          returnValue = num;
          break;
        case OBJECT:
          JSONObject objNode = buildObjectNode(nodePath, propertyDef);
          returnValue = objNode;
          break;
        case STRING:
          int minChars = 5;
          int maxChars = 20;
          if (propertyDef.containsKey("minLength")) {
            minChars = Integer.parseInt(propertyDef.get("minLength").toString());
          }
          if (propertyDef.containsKey("maxLength")) {
            maxChars = Integer.parseInt(propertyDef.get("maxLength").toString());
          }
          String alphaNum = DataGenerator.generateAlphaNumericString(minChars, maxChars);
          returnValue = alphaNum;
          break;
        case URI:
          String uri = DataGenerator.generateRandomURIRef();
          returnValue = uri;
          break;
        case URI_REF:
          String uri_ref = DataGenerator.generateRandomURIRef();
          returnValue = uri_ref;
          break;
        case BYTE:
          returnValue = null;
          break;
        default:
          returnValue = "Unknown ModelPropertyType";
      }

      if (parentPath != null) {// not dealing with root so don't add value
        setMetadataValue(type, parentPath, key, returnValue);
      }

      return returnValue;
    }
  }

  /**
   * Method to get the path to schemas external to the schema for this object, but referenced.
   * 
   * @param controlPath path to this schema
   * @param relativePath relative path to referenced schema
   * @return String
   */
  protected String getRelativeResourceLocation(String controlPath, String relativePath) {
    String returnPath = "";
    String[] relativeSegments = relativePath.split("\\.\\.");
    int relSegLength = relativeSegments.length;
    if (relSegLength == 1) {
      returnPath = "/schemas/" + relativePath;
    } else {
      String[] controlComponents = controlPath.split("/");
      String[] relativeComponents = relativePath.split("/");
      int controlCount = controlComponents.length;
      int relativeCount = relativeComponents.length;

      int startingPoint = controlCount - relativeCount;
      String relativeReturn = relativePath.replace("../", "");
      for (int i = 0; i <= startingPoint; i++) {
        returnPath += controlComponents[i] + "/";
      }
      returnPath += relativeReturn;
    }
    return returnPath;
  }

  /**
   * Method to sync the JSONObject type objectMetadata.
   * 
   * @param dataTree
   */
  private void refreshMetadata(JsonNode dataTree) {
    String treeDump = dataTree.toString().replace("\\", "").replace("\"[", "[").replace("]\"", "]");
    // strip backslashes and format arrays. A problem caused by the JsonNode string
    // dump.
    try {
      objectMetadata = (JSONObject) parser.parse(treeDump);
    } catch (ParseException e) {
      logger.log(Level.SEVERE, e.getMessage());
    }
  }

  /**
   * Method to cast and post value to json node path.
   * 
   * @param type ModelPropertyType
   * @param pathToParent String Fully qualified path to parent node of the target
   * @param key String target node
   * @param value Object value to be assigned
   */
  protected void setMetadataValue(ModelPropertyType type, String pathToParent, String key,
      Object value) {
    JsonNode tree = null;
    pathToParent = (pathToParent == "/") ? "" : pathToParent;
    try {

      tree = mapper.readTree(objectMetadata.toJSONString());
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getMessage());
    }
    if (type == ModelPropertyType.BOOLEAN) {
      ((ObjectNode) tree.at(pathToParent)).put(key, Boolean.parseBoolean(value.toString()));
    } else if (type == ModelPropertyType.INTEGER) {
      ((ObjectNode) tree.at(pathToParent)).put(key, Long.parseLong(value.toString()));
    } else if (type == ModelPropertyType.NUMBER) {
      ((ObjectNode) tree.at(pathToParent)).put(key, Double.parseDouble(value.toString()));
    } else if (type == ModelPropertyType.OBJECT || type == ModelPropertyType.REF_DEFINITION
        || type == ModelPropertyType.REF_SCHEMA) {
      try {
        String stringValue = value.toString();
        JsonNode node = mapper.readTree(stringValue);
        ((ObjectNode) tree.at(pathToParent)).set(key, node);
      } catch (IOException e) {
        logger.log(Level.SEVERE, e.getMessage());
      }
    } else if (type == ModelPropertyType.NULL) {
      ((ObjectNode) tree.at(pathToParent)).putNull(key);
    } else {

      ((ObjectNode) tree.at(pathToParent)).put(key, value.toString());
    }
    refreshMetadata(tree);
  }

  /**
   * Returns path to the Object.
   * 
   * @return String
   */
  public String getObjectPath() {
    return objectPath;
  }

  /**
   * Sets path to the Object.
   * 
   * @param objectPath path to the object
   */
  public void setObjectPath(String objectPath) {
    this.objectPath = objectPath;
  }

  /**
   * Returns reference to the data generated for an instance of the Object Type given.
   * 
   * @return JSONObject
   */
  public JSONObject getObjectMetadata() {
    return objectMetadata;
  }

  /**
   * Manually sets the metadata for an object instance.
   * 
   * @param data JSONObject
   */
  public void setObjectMetadata(JSONObject data) {
    objectMetadata = data;
  }

  /**
   * Returns items for a model of type Array
   * 
   * @return JSONArray
   */
  public JSONArray getObjectItems() {
    return objectItems;
  }

  /**
   * Manually sets the metadata for an array instance.
   * 
   * @param data JSONArray to be used for a ModelObject instance of type Array
   */
  public void setObjectItems(JSONArray data) {
    objectItems = data;
  }

  /**
   * Data to be set on specified index of items on a ModelObject of type Array.
   * 
   * @param index position at which the item is to be set
   * @param data data to be set
   */
  @SuppressWarnings("unchecked")
  public void setDataAtItemsIndex(int index, JSONObject data) {
    if (modelType == ModelPropertyType.ARRAY) {
      objectItems.set(index, data);
    } else {
      Exception exception = new SchemaTypeException(ModelPropertyType.ARRAY);
      logger.log(Level.SEVERE, "An exception was thrown", exception);
    }
  }

  /**
   * Get data at specific index of objectItems on a ModelObject of type Array.
   * 
   * @param index position of the item
   * @return JSONObject
   */
  public JSONObject getDataAtItemsIndex(int index) {
    if (modelType == ModelPropertyType.ARRAY) {
      return (JSONObject) objectItems.get(index);
    } else {
      Exception exception = new SchemaTypeException(ModelPropertyType.ARRAY);
      logger.log(Level.SEVERE, "An exception was thrown", exception);
      return null;
    }
  }

  /**
   * Returns objectName
   * 
   * @return String
   */
  public String getObjectName() {
    return objectName;
  }

  /**
   * Sets the name of the object. This field is often used in the default for a path segment in a
   * REST api.
   * 
   * @param objectName name to be used as a string representation of the object (often used in REST
   *        path segments)
   */
  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }

  /**
   * Returns type of object as defined in the ride object types.
   * 
   * @return String
   * @see ModelPropertyType
   */
  public String getObjectType() {
    return objectType;
  }

  /**
   * Returns location of the schema in the project resources.
   * 
   * @return String
   */
  public String getModelResourceLocation() {
    return resourceLocation;
  }

  /**
   * Method to set value of existing key in the metadata.
   * 
   * @param property name of the key to be reset
   * @param value new value of the key
   */
  @SuppressWarnings("unchecked")
  public void setObjectMetadataProperty(String property, Object value) {
    if (objectMetadata.containsKey(property)) {
      objectMetadata.remove(property);
    }
    objectMetadata.put(property, value);
  }

  /**
   * * Method to get value of key in the Object metadata.
   * 
   * @param property string representation of the property to be retrieved
   * @return Object representation of the value
   */
  public Object getObjectMetadataProperty(String property) {
    return objectMetadata.get(property);
  }

  /**
   * Get value of node at given path.
   * 
   * @param path standard path to the node in the object instance to be retrieved
   * @return Object
   */
  public Object getObjectMetadataValueAt(String path) {
    JsonNode node = null;
    try {
      node = mapper.readTree(objectMetadata.toJSONString());
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getMessage());
      e.printStackTrace();
    }

    return node.at(path);
  }

  /**
   * Method to remove a key/value pair the Object metadata.
   * 
   * @param property String representation of the property to be removed
   * @return boolean indicator of whether the property existed previously
   */
  public boolean removeObjectMetadataProperty(String property) {
    boolean existed = false;
    if (objectMetadata.containsKey(property)) {
      objectMetadata.remove(property);
      existed = true;
    }
    return existed;
  }
}
