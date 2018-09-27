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

/**
 * 
 * @author tedcasey
 *
 */
public class ArrayObject extends ModelObject {

  public ArrayObject(String objectName, boolean initRequiredOnly) {
    super(Service.SAMPLE_SERVICE.toString(), CloudObjectType.ARRAY_OBJECT.toString(), objectName,
        initRequiredOnly);
    buildValidModelInstance();
  }

  public JSONObject getItemAt(int index) {
    return getDataAtItemsIndex(index);
  }

  public void setItemAt(int index, JSONObject item) {
    setDataAtItemsIndex(index, item);
  }
}
