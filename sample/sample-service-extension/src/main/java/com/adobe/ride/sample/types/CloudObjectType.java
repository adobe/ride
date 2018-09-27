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

package com.adobe.ride.sample.types;

/**
 * An enumeration of the object types used by the sample ride extension.
 * 
 * @author mstockbr, tedcasey
 *
 */
public enum CloudObjectType {
  ARRAY_OBJECT("array_object"), UBER_OBJECT("uber_object"), OBJECT1("object1"), OBJECT2(
      "object2"), PROFILE_OBJECT("profile");

  private final String value;

  private CloudObjectType(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static CloudObjectType parse(String type) {
    for (CloudObjectType t : CloudObjectType.values()) {
      if (t.value.equalsIgnoreCase(type)) {
        return t;
      }
    }
    return null;
  }
}
