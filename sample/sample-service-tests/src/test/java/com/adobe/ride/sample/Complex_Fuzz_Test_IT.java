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

import java.util.UUID;

import org.testng.annotations.Factory;
import com.adobe.ride.libraries.fuzzer.MetadataFuzzer;
// import com.adobe.ride.sample.cloud_objects.SampleServiceObject1;
import com.adobe.ride.sample.cloud_objects.ProfileObject;
import com.adobe.ride.sample.types.Service;

/**
 * 
 * @author tedcasey
 *
 */
public class Complex_Fuzz_Test_IT {

  @Factory
  public Object[] fuzzObjectMetadata_IT() throws Exception {
    String itemName = UUID.randomUUID().toString();
    ProfileObject object1 = new ProfileObject(itemName, false);
    return new Object[] {new MetadataFuzzer(Service.SAMPLE_SERVICE.toString(), object1)};
  }
}
