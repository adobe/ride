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

package com.adobe.ride.utilities.model.exceptions;

import org.json.simple.JSONObject;

/**
 * @author tedcasey
 * 
 */
public class UnexpectedSchemaTypeException extends ServiceException {

  private static final long serialVersionUID = 3229815211562219065L;
  private JSONObject targetObject;

  /**
   * @param obj JSONObject with unexpected format
   */
  public UnexpectedSchemaTypeException(JSONObject obj) {
    super("Encountered unexpected or null schema type when attempting to load model: "
        + obj.toString());
    this.targetObject = obj;
  }

  /**
   * Returns the JSONObject associated with the error.
   * 
   * @return object which generated the error.
   */
  public JSONObject getTargetObject() {
    return targetObject;
  }
}
