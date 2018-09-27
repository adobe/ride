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

package com.adobe.ride.core.globals;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.UUID;

/**
 * List of common test constants
 *
 * @author mstockbr, nkrishna
 */
public class Constants {

  public static final String DEFAULT_CLIENT_NAME = "TBD";
  public static final String DEFAULT_CLIENT_VERSION = "0.0.1";
  public static final String DEFAULT_PRODUCT_ID = "your domain";
  public static final String DEFAULT_CONTENT_TYPE = "application/json;charset=UTF-8";
  public static final String JSON_CONTENT_NO_CHARSET_TYPE = "application/json";
  public static final String PROTOBUF_CONTENT_TYPE = "application/octet-stream";

  // Test Run Profile Names
  public static final String SMOKE = "smoke";
  public static final String ACCEPTANCE = "acceptance";
  public static final String CERTIFICATION = "certification";
  public static final String INTEGRATION = "integration";
  public static final String PERFORMANCE = "performance";

  private static long defaultSessionID = (new Random()).nextLong();
  private static int defaultRequestID = 0;

  public static long defaultSessionID() {
    return defaultSessionID;
  }

  public static long generateNewSessionID() {
    defaultSessionID = (new Random(defaultSessionID)).nextLong();
    return defaultSessionID;
  }

  public static int defaultRequestID() {
    return defaultRequestID++;
  }

  public static UUID fromLong(long id) {
    byte[] bytes = ByteBuffer.allocate(Long.SIZE).putLong(id).array();
    return UUID.nameUUIDFromBytes(bytes);
  }

  public static UUID fromInt(int id) {
    byte[] bytes = ByteBuffer.allocate(Integer.SIZE).putInt(id).array();
    return UUID.nameUUIDFromBytes(bytes);
  }
}
