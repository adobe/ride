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

package com.adobe.ride.utilities.model.types;

/**
 * Enumeration of types used to create metadata based on json-based schema
 *
 * @author tedcasey
 * 
 */
public enum ModelPropertyType {
  PATTERN, ANYOF, ARRAY, BOOLEAN, BYTE, DATE, DATETIME, EMAIL, REF_DEFINITION, REF_SCHEMA, REF_DATA, ENUM, INTEGER, IPV4, IPV6, NULL, NUMBER, OBJECT, STRING, SYNC, TIMESTAMP, URI, URI_REF;

  /**
   * Return enum based on eval of string value
   * 
   * @param string value 
   * @return ModelPropertyType
   */
  public static ModelPropertyType eval(String string) {
    for (ModelPropertyType t : ModelPropertyType.values()) {
      if (string.equalsIgnoreCase(t.toString())) {
        return t;
      }
    }
    return null;
  }
}
