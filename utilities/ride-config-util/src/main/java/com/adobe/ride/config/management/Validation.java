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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.IncompleteArgumentException;

/**
 * 
 * @author hook
 *
 */
public class Validation {
  private static Pattern uuidGuidWithDashesPattern = Pattern.compile(
      "(?!0{6})[A-Fa-f0-9]{8}\\-(?!0{6})[A-Fa-f0-9]{4}\\-(?!0{6})[A-Fa-f0-9]{4}\\-(?!0{6})[A-Fa-f0-9]{4}\\-(?!0{6})[A-Fa-f0-9]{12}",
      Pattern.CASE_INSENSITIVE);
  private static Pattern uuidGuidPattern =
      Pattern.compile("[A-Fa-f0-9]{32}", Pattern.CASE_INSENSITIVE);

  protected Validation() {}

  /**
   * Check for empty or null parameter.
   *
   * @param <T> the generic type
   * @param parameter the parameter
   */
  public static <T> void isNullOrEmptyParameter(T parameter) {
    if (parameter == null) {
      throw new IllegalArgumentException("A parameter value has been assigned a null value");
    }

    if (parameter.getClass().equals(String.class) && parameter.toString().isEmpty()) {
      throw new IllegalArgumentException("A parameter value has been assigned an empty value");
    }
  }

  /**
   * Checks if is null or empty parameter.
   * 
   * @param <T> the generic type
   * @param parameterName the name of the parameter that is missing the incoming value
   * @param parameter the actual parameter to test if null or empty
   */
  public static <T> void isNullOrEmptyParameter(String parameterName, T parameter) {
    if (parameter == null) {
      throw new IllegalArgumentException(
          String.format("%s parameter has been assigned a null value", parameterName));
    }

    if (parameter.getClass().equals(String.class) && parameter.toString().isEmpty()) {
      throw new IllegalArgumentException(
          String.format("%s parameter has been assigned an empty value", parameterName));
    }
  }

  /**
   * Checks if is null or empty list.
   *
   * @param <T> the generic type
   * @param list the list
   */
  public static <T> void isNullOrEmptyList(List<T> list) {
    isNullList(list);

    if (list.size() == 0) {
      throw new NullOrEmptyListException("Incoming parameter list is empty");
    }
  }

  /**
   * Checks if is null or empty list.
   *
   * @param <T> the generic type
   * @param listName the name of the list name
   * @param list the list
   */
  public static <T> void isNullOrEmptyList(String listName, List<T> list) {
    isNullList(list);

    if (list.size() == 0) {
      throw new NullOrEmptyListException(
          String.format("Incoming parameter list %s is empty", listName));
    }
  }

  /**
   * Checks if is null or empty map.
   *
   * @param <T> the generic type
   * @param <K> the key type
   * @param map the map
   */
  public static <T, K> void isNullOrEmptyMap(Map<T, K> map) {
    isNullMap(map);

    if (map.size() == 0) {
      throw new NullOrEmptyListException("Incoming parameter map is empty");
    }
  }

  /**
   * Checks if is null or empty map.
   *
   * @param <T> the generic type
   * @param <K> the key type
   * @param mapName the map name
   * @param map the map
   */
  public static <T, K> void isNullOrEmptyMap(String mapName, Map<T, K> map) {
    isNullMap(map);

    if (map.size() == 0) {
      throw new NullOrEmptyListException(
          String.format("Incoming parameter map %s is empty", mapName));
    }
  }

  /**
   * Checks if is null map.
   *
   * @param <T> the generic type
   * @param <K> the key type
   * @param map the map
   */
  public static <T, K> void isNullMap(Map<T, K> map) {
    if (map == null) {
      throw new NullOrEmptyListException("Incoming parameter map is null");
    }
  }

  /**
   * Checks if is null list.
   *
   * @param <T> the generic type
   * @param list the list
   */
  public static <T> void isNullList(List<T> list) {
    if (list == null) {
      throw new NullOrEmptyListException("Incoming parameter list is null");
    }
  }

  /**
   * Represents the case where a method takes in a parameter that has a number of properties, some
   * of which have not been set.
   *
   * @param <T> the generic type
   * @param property the property value to validate against
   */
  public static <T> void isIncompleteArgument(T property) {
    if (property == null) {
      throw new IncompleteArgumentException(
          "A property of an incoming parameter has been assigned a null value");
    }

    if (property.getClass().equals(String.class) && property.toString().isEmpty()) {
      throw new IncompleteArgumentException(
          "A property of an incoming parameter has been assigned an empty value");
    }
  }

  /**
   * Represents the case where a method takes in a parameter that has a number of properties, some
   * of which have not been set.
   *
   * @param <T> the generic type
   * @param parameterName the name of the actual parameter to which the property belongs
   * @param propertyName the property name
   * @param property the property value to validate against
   */
  public static <T> void isIncompleteArgument(String parameterName, String propertyName,
      T property) {
    if (property == null) {
      throw new IncompleteArgumentException(String.format(
          "%s::%s is incomplete and has been assigned a null value", parameterName, propertyName));
    }

    if (property.getClass().equals(String.class) && property.toString().isEmpty()) {
      throw new IncompleteArgumentException(
          String.format("%s::%s is incomplete and has been assigned an empty value", parameterName,
              propertyName));
    }
  }

  /**
   * Validate if an incoming universally unique identifier is in the correct format (GUID/UUID).
   *
   * @param identifier the identifier can be in the form (A-Z, a-z or 0-9) 36 (with dashes)
   * @return true, if the pattern is a complete match, otherwise return value
   */
  public static boolean isValidUUID(String identifier) {
    if (StringUtilities.isEmpty(identifier)) {
      return false;
    }

    Matcher matcher = uuidGuidWithDashesPattern.matcher(identifier);
    return matcher.matches();
  }

  /**
   * Validate if an incoming universally unique identifier is in the correct format (GUID/UUID).
   *
   * @param identifier the identifier can be in the form (A-Z, a-z or 0-9) 32 (**without** dashes)
   * @return true, if the pattern is a complete match, otherwise return value
   */
  public static boolean isValidUUIDWithoutDashes(String identifier) {
    if (StringUtilities.isEmpty(identifier)) {
      return false;
    }

    Matcher matcher = uuidGuidPattern.matcher(identifier);
    return matcher.matches();
  }
}
