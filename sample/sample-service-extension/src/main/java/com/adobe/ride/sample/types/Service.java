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
 * An enumeration of services targeted by the sample extension.
 * 
 * @author mstockbr, tedcasey
 *
 */
public enum Service {
  SAMPLE_SERVICE("SampleService");
  // It is possible you'd want to create a group extension with multiple services represented in
  // that group. In that case you'd probably have multiple controllers annotated with different
  // values here.

  private final String value;

  private Service(String value) {
    this.value = value;
  }

  public String toString() {
    return value;
  }

  public static Service parse(String type) {
    for (Service t : Service.values()) {
      if (t.value.equalsIgnoreCase(type)) {
        return t;
      }
    }
    return null;
  }
}
