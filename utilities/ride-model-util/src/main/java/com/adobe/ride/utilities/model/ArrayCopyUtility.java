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

package com.adobe.ride.utilities.model;

/**
 * A class containing static utility methods.
 *
 * @author stlarson
 *
 */

public class ArrayCopyUtility {

  /**
   * Copies an array using an interim temporary array so that the original internal representation
   * isn't stored in an external mutable object.
   * 
   * @param source The source array to be copied
   * @return String[] A string array containing the copied source array.
   */
  public static String[] copyStringArray(String[] source) {
    if (source != null) {
      String[] dest = new String[source.length];
      System.arraycopy(source, 0, dest, 0, source.length);
      return dest;
    }
    return null;
  }

  /**
   * Copies a byte array using an interim temporary array so that the original internal
   * representation isn't stored in an external mutable object.
   * 
   * @param source The source array to be copied
   * @return byte[] A byte array containing the copied source array
   */
  public static byte[] copyArray(byte[] source) {
    if (source != null) {
      byte[] dest = new byte[source.length];
      System.arraycopy(source, 0, dest, 0, source.length);
      return dest;
    }
    return null;
  }
}
