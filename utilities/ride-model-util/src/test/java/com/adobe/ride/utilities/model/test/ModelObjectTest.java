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

package com.adobe.ride.utilities.model.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.adobe.ride.utilities.model.ModelObject;
import com.adobe.ride.utilities.model.exceptions.ModelSearchException;
import com.adobe.ride.utilities.model.exceptions.UnexpectedModelPropertyTypeException;
import com.adobe.ride.utilities.model.types.ModelPropertyType;

/**
 * 
 * @author tedcasey
 * 
 */
public class ModelObjectTest {
  private ModelObject testObj;

  @Test(suiteName = "smoke", groups = "integration", enabled = true)
  public void testInstantiateModel() {
    testObj = new ModelObject("TestService", "article", "pathToObject/objectName", false) {};
    JSONObject model = testObj.getModel();
    Assert.assertTrue(model.containsKey("properties"));
  }

  @SuppressWarnings("unchecked")
  @Test(suiteName = "smoke", groups = "integration", enabled = true,
      dependsOnMethods = {"testInstantiateModel"})
  public void testBuildObjectInstance() {
    testObj.setObjectMetadataProperty("entityName", "FrederickTheGreat");
    testObj.buildValidModelInstance();
    ModelObject.prettyPrintToConsole(testObj.getObjectMetadata());
    Set<String> test = testObj.getObjectMetadata().keySet();
    Set<String> control = testObj.getModelProperties().keySet();
    Assert.assertTrue(test.containsAll(control));
  }


  @SuppressWarnings("unchecked")
  @Test(suiteName = "smoke", groups = "integration", enabled = true,
      dependsOnMethods = {"testInstantiateModel"})
  public void testBuildArrayInstance() throws ModelSearchException {
    ModelObject testArray =
        new ModelObject("TestService", "profile", null, false);
    JSONArray items = (JSONArray) testArray.buildValidModelInstance();
    ModelObject.prettyPrintToConsole(items);
    JSONArray objectItems = testArray.getObjectItems();
    Set<String> element1 = ((JSONObject) objectItems.get(0)).keySet();
    Set<String> control = testArray.getModelProperties().keySet();
    Assert.assertTrue(element1.containsAll(control));
  }

  @SuppressWarnings("unchecked")
  @Test(suiteName = "smoke", groups = "integration", enabled = true)
  public void testBuildArraySchemaDefinitionModelInstance() throws ModelSearchException {
    ModelObject responseObj =
        new ModelObject("TestService", "response_error", "pathToObject/objectName", false);
    JSONObject model = responseObj.getModel();
    Assert.assertTrue(model.containsKey("properties"));
    responseObj.buildValidModelInstance();
    ModelObject.prettyPrintToConsole(responseObj.getObjectMetadata());
    Set<String> test = responseObj.getObjectMetadata().keySet();
    Set<String> control = responseObj.getModelProperties().keySet();
    Assert.assertTrue(test.containsAll(control));
  }

  @SuppressWarnings("unchecked")
  @Test(suiteName = "smoke", groups = "integration", enabled = true)
  public void testStringJsonSchema() throws ModelSearchException {

    String modelString = "";
    // pull in model as String
    try {
      InputStream inputStream =
          ClassLoader.class.getResourceAsStream("/schemas/TestService/simple.json");
      Charset nullCharset = null; // platform default
      modelString = IOUtils.toString(inputStream, nullCharset);
    } catch (IOException e) {
      e.printStackTrace();
    }

    ModelObject responseObj = new ModelObject(modelString, false);
    JSONObject model = responseObj.getModel();
    Assert.assertTrue(model.containsKey("properties"));
    responseObj.buildValidModelInstance();
    ModelObject.prettyPrintToConsole(responseObj.getObjectMetadata());
    Set<String> test = responseObj.getObjectMetadata().keySet();
    Set<String> control = responseObj.getModelProperties().keySet();
    Assert.assertTrue(test.containsAll(control));
  }

  @SuppressWarnings("unchecked")
  @Test(suiteName = "smoke", groups = "integration", enabled = true)
  public void testPresetsAndBuildObjectInstance() throws ModelSearchException {
    JSONObject presets = new JSONObject();
    JSONObject authorInfo = new JSONObject();
    authorInfo.put("name", "Gladys Phillips");
    authorInfo.put("authorUrl", "www.gladysphillips.com");
    presets.put("authorInfo", authorInfo);
    ModelObject testObj =
        new ModelObject("TestService", "article", null, presets, false);
    JSONObject model = testObj.getModel();
    Assert.assertTrue(model.containsKey("properties"));
    testObj.buildValidModelInstance();
    ModelObject.prettyPrintToConsole(testObj.getObjectMetadata());
    Set<String> test = testObj.getObjectMetadata().keySet();
    Set<String> control = testObj.getModelProperties().keySet();
    Assert.assertTrue(test.containsAll(control));
  }
  
  @SuppressWarnings("unchecked")
  @Test(suiteName = "smoke", groups = "integration", enabled = true)
  public void testPresetsAndBuildProfile2Object() throws ModelSearchException {
    JSONObject presets = new JSONObject();
    JSONObject firstNameObject = new JSONObject();
    firstNameObject.put("firstName",  "user1");
    JSONObject nameObject = new JSONObject();
    nameObject.put("name",  firstNameObject);
    presets.put("person",  nameObject);
    ModelObject testObj =
        new ModelObject("TestService", "profile2", null, presets, false);
    JSONObject model = testObj.getModel();
    Assert.assertTrue(model.containsKey("properties"));
    testObj.buildValidModelInstance();
    ModelObject.prettyPrintToConsole(testObj.getObjectMetadata());
    Set<String> test = testObj.getObjectMetadata().keySet();
    Set<String> control = testObj.getModelProperties().keySet();
    Assert.assertTrue(test.containsAll(control));
  }

  @SuppressWarnings("unchecked")
  @Test(suiteName = "smoke", groups = "integration", enabled = true)
  public void testGetMappedPath() throws ModelSearchException {
    String controlValue = "Gladys Phillips";
    JSONObject presets = new JSONObject();
    JSONObject authorInfo = new JSONObject();
    authorInfo.put("name", "Gladys Phillips");
    authorInfo.put("authorUrl", "www.gladysphillips.com");
    presets.put("authorInfo", authorInfo);
    ModelObject testObj =
        new ModelObject("TestService", "article", null, presets, false);
    JSONObject model = testObj.getModel();
    Assert.assertTrue(model.containsKey("properties"));
    testObj.buildValidModelInstance();
    ModelObject.prettyPrintToConsole(testObj.getObjectMetadata());

    String retrievedValue = testObj.getObjectMetadataValueAt("/authorInfo/name").toString();
    String testValue = retrievedValue.replace("\"", "");
    Assert.assertTrue(testValue.equals(controlValue));
  }

  @SuppressWarnings("unchecked")
  @Test(suiteName = "smoke", groups = "integration", enabled = true)
  public void testPresetsAndBuildTargetNodes() throws ModelSearchException {
    // Presets
    JSONObject presets = new JSONObject();
    JSONObject authorInfo = new JSONObject();
    authorInfo.put("name", "Gladys Phillips");
    authorInfo.put("authorUrl", "www.gladysphillips.com");
    presets.put("authorInfo", authorInfo);

    // Targets
    Set<String> targetNodes = new HashSet<String>();
    targetNodes.add("/_links/contentUrl");
    targetNodes.add("/_links/relatedContent");
    targetNodes.add("/language");

    // Instantiate
    ModelObject testObj =
        new ModelObject("TestService", "article", null, presets, targetNodes);
    JSONObject model = testObj.getModel();
    Assert.assertTrue(model.containsKey("properties"));

    // Gen data
    testObj.buildValidModelInstance();

    // Dump To Console
    ModelObject.prettyPrintToConsole(testObj.getObjectMetadata());

    Set<String> control = targetNodes;
    control.add("authorInfo");
    Assert.assertTrue(control.containsAll(control));
    Assert.assertFalse(control.contains("isAd"));
  }

  @SuppressWarnings("unchecked")
  @Test(suiteName = "smoke", groups = "integration", enabled = true)
  public void testPresetsAndBuildNumberOfInstances() throws ModelSearchException {
    // Presets
    JSONObject presets = new JSONObject();
    JSONObject authorInfo = new JSONObject();
    authorInfo.put("name", "Tom Jones");
    authorInfo.put("authorUrl", "www.tomjones.com");
    presets.put("authorInfo", authorInfo);

    // Targets
    Set<String> targetNodes = new HashSet<String>();
    targetNodes.add("/_links/contentUrl");
    targetNodes.add("/_links/relatedContent");
    targetNodes.add("/language");

    // Instantiate
    ModelObject testObj =
        new ModelObject("TestService", "article", null, presets, targetNodes);
    JSONObject model = testObj.getModel();
    Assert.assertTrue(model.containsKey("properties"));
    JSONArray objects = testObj.generateModelInstances(10);
    ModelObject.prettyPrintToConsole(objects);
  }

  @Test(suiteName = "smoke", groups = "integration", enabled = true)
  public void testPrimitiveSchemaDefinitionModelInstance() {
    final String schema = "{\"type\":\"integer\", \"format\":\"int32\"}";
    final ModelObject modelObject = new ModelObject(schema, false);
    final Object modelInstance = modelObject.buildValidModelInstance();
    Assert.assertTrue(modelInstance instanceof Long);
  }

  @Test(suiteName = "smoke", groups = "integration", enabled = true)
  public void testFallBackToSchemaTypeIfFormatCannotBeIdentified()
      throws UnexpectedModelPropertyTypeException {
    String schema = "{\"type\":\"integer\", \"format\":\"int32\"}";
    ModelObject modelObject = new ModelObject(schema, false);
    JSONObject model = modelObject.getModel();
    Assert.assertEquals(ModelObject.getModelPropertyType(model), ModelPropertyType.INTEGER);

    schema = "{\"type\":\"string\", \"format\":\"binary\"}";
    modelObject = new ModelObject(schema, false);
    model = modelObject.getModel();
    Assert.assertEquals(ModelObject.getModelPropertyType(model), ModelPropertyType.STRING);
  }

  // TODO: Implement this test
  /*-
  @Test(suiteName = "smoke", groups = "integration", enabled = true,
      dependsOnMethods = {"testInstantiateModel"})
  public void testNumberGenerators() {}
  */
}
