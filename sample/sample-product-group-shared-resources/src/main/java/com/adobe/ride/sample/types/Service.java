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
 * @author tedcasey
 */
public class Service {
  public static final String SAMPLE_SERVICE = "SampleService";
  public static final String OTHER_SAMPLE_SERVICE = "OtherSampleService";

  public static String[] values() {
    String[] return_array = {SAMPLE_SERVICE, OTHER_SAMPLE_SERVICE};
    return return_array;
  }
}
