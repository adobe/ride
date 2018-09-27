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

package com.adobe.ride.config.management;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * 
 * @author hook
 *
 */
public class StringUtilities {

  public static final String SECTIONS_MIN_VIEWER_VERSION = "22.0.0";
  public static final String IS_TRUSTED_CONTENT_MIN_VIEWER_VERSION = "25.0.0";

  public static boolean isEmpty(String value) {
    return (value == null || value.trim().isEmpty());
  }

  public static long safeParseLong(final Object value, final long defaultValue) {

    if (value == null) {
      return defaultValue;
    }

    if (value.getClass().equals(String.class)) {
      if (value.toString().isEmpty()) {
        return defaultValue;
      }

      return Long.parseLong((String) value);
    }

    if (value.getClass().equals(Integer.class)) {
      return (Integer) value;
    }

    if (value.getClass().equals(Long.class)) {
      return (Long) value;
    }

    return defaultValue;
  }


  public static int safeParseInteger(final String value, final int defaultValue) {

    return (isEmpty(value)) ? defaultValue : Integer.parseInt(value);
  }

  public static boolean safeParseBoolean(final String value, final boolean defaultValue) {
    return (isEmpty(value)) ? defaultValue : Boolean.parseBoolean(value);
  }

  @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING",
      justification = "This was not authored by QE and may be refactored at a later time.")
  public static boolean isStringValidUTF8(String input) {
    Validation.isNullOrEmptyParameter("input", input);

    Charset encoding = StandardCharsets.UTF_8;
    CharsetDecoder cs = Charset.forName(encoding.name()).newDecoder();

    try {
      cs.decode(ByteBuffer.wrap(input.getBytes()));
      return true;
    } catch (CharacterCodingException e) {
      return false;
    }
  }
}
