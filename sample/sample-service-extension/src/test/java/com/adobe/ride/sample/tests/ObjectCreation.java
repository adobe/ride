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

package com.adobe.ride.sample.tests;

import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;

import com.adobe.ride.sample.cloud_objects.SampleServiceObject1;
import com.adobe.ride.sample.cloud_objects.SampleServiceObject2;
import com.adobe.ride.utilities.model.exceptions.UnexpectedModelDefinitionException;
import com.adobe.ride.utilities.model.exceptions.UnexpectedModelPropertyTypeException;
import com.adobe.ride.utilities.model.exceptions.UnexpectedSchemaTypeException;

/**
 * 
 * @author tedcasey
 *
 */
public class ObjectCreation {

  @Test
  public void testCreateObject1Type()
      throws UnexpectedModelPropertyTypeException, UnexpectedModelDefinitionException, IOException,
      ParseException, UnexpectedSchemaTypeException {
    SampleServiceObject1 testObj = new SampleServiceObject1("object/object1", false);

    testObj.dumpObject();

    Assert.assertNotNull(testObj.getObjectMetadata());
  }

  @Test
  public void testCreateObject2Type()
      throws UnexpectedModelPropertyTypeException, UnexpectedModelDefinitionException, IOException,
      ParseException, UnexpectedSchemaTypeException {
    SampleServiceObject2 testObj = new SampleServiceObject2("/object/object2", false);

    testObj.dumpObject();

    Assert.assertNotNull(testObj.getObjectMetadata());
  }
}
