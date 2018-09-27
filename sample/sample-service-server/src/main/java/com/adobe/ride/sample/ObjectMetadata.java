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

package com.adobe.ride.sample;

import com.adobe.ride.sample.types.SampleServiceObjectType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author boshea, tedcasey
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjectMetadata {

  private SampleServiceObjectType type;
  public static final String TYPE = "type";

  private String name;
  public static final String NAME = "name";

  private int objectCode;
  public static final String OBJECT_CODE = "objectCode";

  private String thisProperty;
  public static final String THIS_PROPERTY = "thisProperty";

  private String thatProperty;
  public static final String THAT_PROPERTY = "thatProperty";

  public ObjectMetadata() {}

  public SampleServiceObjectType getType() {
    return type;
  }

  public void setType(SampleServiceObjectType type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getObjectCode() {
    return objectCode;
  }

  public void setObjectCode(int objectCode) {
    this.objectCode = objectCode;
  }

  public String getThisProperty() {
    return thisProperty;
  }

  public void setThisProperty(String thisProperty) {
    this.thisProperty = thisProperty;
  }

  public String getThatProperty() {
    return thatProperty;
  }

  public void setThatProperty(String thatProperty) {
    this.thatProperty = thatProperty;
  }

}
