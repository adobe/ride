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

package com.adobe.ride.sample.cloud_objects;

import org.json.simple.JSONObject;

import com.adobe.ride.sample.types.CloudObjectType;
import com.adobe.ride.sample.types.Service;
import com.adobe.ride.utilities.model.ModelObject;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Notes: This is SampleUberObject, which you may want to have for your extension, if you have a lot
 * of repetitive methods in your objects. Because of encapsultation, in order to get to the
 * ModelObject methods you may need in your subclasses, you may want to override some of the methods
 * in the superclass (modelobject), but you don't have to do it this way. It may make more sense to
 * have all of your objects draw directly from the ModelObject.
 * 
 * @author tedcasey
 *
 */
public class SampleServiceUberObject extends ModelObject {

  protected JSONObject remoteMetadata;

  public SampleServiceUberObject(CloudObjectType type, String objectName,
      boolean initRequiredOnly) {
    super(Service.SAMPLE_SERVICE.toString(), type.toString(), objectName, initRequiredOnly);
    buildValidModelInstance();
  }

  public JSONObject getRemoteProperties() {
    return remoteMetadata;
  }

  public void syncDataToServer() {
    setObjectMetadata(remoteMetadata);
  }

  public void dumpObject() throws JsonProcessingException {
    prettyPrintToConsole(getObjectMetadata());
  }

  public String getPath() {
    return getObjectPath();
  }

  public void setPath(String objectPath) {
    setObjectPath(objectPath);
  }

  public JSONObject getMetadata() {
    return getObjectMetadata();
  }

  public void setMetadata(JSONObject data) {
    setObjectMetadata(data);
  }

  public String getName() {
    return super.getObjectName();
  }

  public void setName(String objectName) {
    super.setObjectName(objectName);
  }

  public void setProperty(String property, Object value) {
    setObjectMetadataProperty(property, value);
  }

  public Object getProperty(String property) {
    return getObjectMetadataProperty(property);
  }
}
