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

import com.adobe.ride.sample.types.CloudObjectType;

/**
 * 
 * @author tedcasey
 *
 */
public class SampleServiceObject2 extends SampleServiceUberObject {

  public SampleServiceObject2(String objectName, boolean initRequiredOnly) {
    super(CloudObjectType.OBJECT2, objectName, initRequiredOnly);
  }

  public String getThatProperty(String value) {
    return getProperty("thatProperty").toString();
  }

  public void setThatProperty(String value) {
    setProperty("thatProperty", value);
  }

}
