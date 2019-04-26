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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.google.common.net.InetAddresses;
import com.mifmif.common.regex.Generex;

/**
 * Class for generating random values
 * 
 * The member functions here are provided for 3 reasons - standardization of generated values,
 * convenience, and reduction of repetitive code in the ModelObject class.
 * 
 * Additional note: There are some limitations in the Xeger Regex string generation class as
 * outlined here: https://code.google.com/p/xeger/wiki/XegerLimitations, but the amount of
 * functionality currently suits our needs given the relatively small scope of the regex definitions
 * in the Entity schemas.
 * 
 * @author tedcasey
 * 
 */
public abstract class DataGenerator {
  protected static final Logger logger = Logger.getLogger(DataGenerator.class.getName());
  protected static final Random randomGen = new Random();
  protected static JSONParser parser = new JSONParser();
  protected static final String rideDefaultDateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  protected static final String jsonFullDateFormat = "yyyy-MM-dd";
  protected static final String dataPrepend = "test-";
  public static final String genericRegex = "[0-9a-zA-Z.-]{0,62}";
  public static final String genericSmallAlphaRegex = "[a-z]{5,10}";
  public static final String genericSmallRandomNumberRegex = "[0-9]{2}";
  public static final String sampleDomainRegex =
      "(\\.com|\\.net|\\.org|\\.us|\\.biz|\\.ca|\\.eu|\\.cn|\\.uk|\\.gr)";
  public static final String phoneRegex = "(tel\\:\\+1-)\\d{3}-\\d{3}-\\d{4}";

  public static final String IPV4_SIMPLE_REGEX = "[0-9]{1,4}:[0-9]{1,4}:[0-9]{1,4}:[0-9]{1,4}";
  public static final String IPV4_REGEX =
      "(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}";
  public static final String IPV6_HEX4DECCOMPRESSED_REGEX =
      "((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?) ::((?:[0-9A-Fa-f]{1,4}:)*)(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}";
  public static final String IPV6_6HEX4DEC_REGEX =
      "((?:[0-9A-Fa-f]{1,4}:){6,6})(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}";
  public static final String IPV6_HEXCOMPRESSED_REGEX =
      "([0-9A-Fa-f]{1,4}([0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)";
  public static final String IPV6_REGEX = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";
  public static final String[] uriRefRegexSampleArray = {
      "^(ftp:\\/\\/)" + genericSmallAlphaRegex + "(\\.)" + genericSmallAlphaRegex + "(\\.)"
          + genericSmallAlphaRegex + "(\\.)" + genericSmallAlphaRegex + sampleDomainRegex,
      "^(http:\\/\\/)" + genericSmallAlphaRegex + "(\\.)" + genericSmallAlphaRegex + "(\\.)"
          + genericSmallAlphaRegex + "(\\.)" + genericSmallAlphaRegex + sampleDomainRegex,
      "^(ldap:\\/\\/)\\[" + IPV6_REGEX + "\\](\\/c=GB\\?objectClass\\?one)",
      "^(mailto:)" + genericSmallAlphaRegex + "(\\.)" + genericSmallAlphaRegex + "\\@"
          + genericSmallAlphaRegex + sampleDomainRegex,
      "^(news:)" + genericSmallAlphaRegex + "(\\.)" + genericSmallAlphaRegex + "(\\.)"
          + genericSmallAlphaRegex + "(\\.unix)",
      phoneRegex, "^(telnet:\\/\\/)" + IPV4_SIMPLE_REGEX + "\\/" + genericSmallRandomNumberRegex,
      "^(urn\\:)" + genericSmallAlphaRegex + "\\:" + genericSmallAlphaRegex + "\\:"
          + genericSmallAlphaRegex + "(\\:dtd\\:xml\\:4\\.1\\.2)"};

  /**
   * Method to get the default Ride string format for dates.
   * 
   * @return String The default standard for date formatting.
   */
  public static String getDateFormat() {
    return rideDefaultDateFormat;
  }

  /**
   * Method to return formatted String representation of a random date somewhere in between the
   * minimum number of days passed and the maximum.
   * 
   * @param minNumDaysPassed minimum number of days passed
   * @param maxNumDaysPassed maximum number of days passed
   * @return String
   */
  public static String generateRandomPassedDate(int minNumDaysPassed, int maxNumDaysPassed) {
    int diff = generateRandomInt(minNumDaysPassed, maxNumDaysPassed);
    LocalDate rndDate = LocalDate.now().minusDays(diff);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(jsonFullDateFormat);
    return formatter.format(rndDate);
  }

  /**
   * Method to return formatted String representation of a date time the number of days from today
   * given in the argument.
   * 
   * @param daysFromToday argument to determine which default formatted date to generate.
   * @return String
   */
  public static String generateStdDateTime(int daysFromToday) {
    Date now = new Date();
    Date newDate = DateUtils.addDays(now, daysFromToday);
    DateFormat stdDate = new SimpleDateFormat(rideDefaultDateFormat);
    return stdDate.format(newDate);
  }

  /**
   * Generate json-standard formatted date string.
   * 
   * @return String
   */
  public static String getTodayJSONFullDateFormat() {

    Date now = new Date();
    DateFormat fullDate = new SimpleDateFormat(jsonFullDateFormat);
    return fullDate.format(now);
  }


  /**
   * Generate a random email address.
   * 
   * @return String
   */
  public static String generateEmail() {
    return generateAlphaNumericString(8, 12) + "@" + generateURI();
  }

  /**
   * Generate an ipv4 conforming address.
   * 
   * @return String
   */
  public static String generateIPv4() {
    return InetAddresses.fromInteger(randomGen.nextInt()).getHostAddress();
  }

  /**
   * Generate an ipv6 conforming address.
   * 
   * @return String
   */
  public static String generateIPv6() {
    return generateRegexValue(IPV6_REGEX);
  }

  /**
   * Generate random boolean.
   * 
   * @return boolean
   */
  public static boolean generateRandomBoolean() {
    return randomGen.nextBoolean();
  }

  /**
   * Method to generate a String matching a given regex pattern.
   * 
   * @param pattern Regex pattern from which a string will be generated
   * @return String
   */
  public static String generateRegexValue(String pattern) {
    try {
      // Trailing pipe check not yet supported by generex
      String lastChar = pattern.substring(pattern.length() - 2);
      if (lastChar.equals("|)")) {
        pattern.replace("|)", "|\\^\\$)");
      }
      Generex generex = new Generex(pattern);
      String result = generex.random();
      // need to sanitize the result in the case of schema problems
      String sanitizedResult = result.replace("^", "").replace("$", "");
      return sanitizedResult;
    } catch (Exception e) {
      logger.warning("Could not generate string for regex: " + pattern);
      return "";
    }
  }

  /**
   * Method to return Unix timestamp of the current time.
   * 
   * @return String
   */
  public static String generateTimestamp() {
    long unixTimestamp = Instant.now().getEpochSecond();
    return String.valueOf(unixTimestamp);
  }

  /**
   * Generate random int between given values.
   * 
   * @param min minimum possible int to be returned
   * @param max maximum possible int to be returned
   * 
   * @return int
   */
  public static int generateRandomInt(int min, int max) {
    return randomGen.nextInt((max - min) + 1) + min;
  }

  /**
   * Generate random int between given values. Additional args to define whether min and max are
   * exclusive.
   * 
   * @param min minimum possible int to be returned
   * @param max maximum possible int to be returned
   * 
   * @param exclusiveMin If true, value returned will be greater than min
   * @param exclusiveMax if true, value returned will be less than max
   * 
   * @return int
   */
  public static int generateRandomInt(int min, int max, boolean exclusiveMin,
      boolean exclusiveMax) {
    int value = generateRandomInt(min, max);
    if ((exclusiveMin && value == min) || (exclusiveMax && value == min)) {
      value = generateRandomInt(min, max, exclusiveMin, exclusiveMax);
    }
    return value;
  }

  /**
   * Generate random int between given values.
   *
   * @param min minimum possible int to be returned
   * @param max maximum possible int to be returned
   *
   * @return int
   */
  public static int generateRandomNumber(int min, int max) {
    return generateRandomInt(min, max);
  }

  /**
   * Generate random int between given values. Additional args to define whether min and max are
   * exclusive.
   * 
   * @param min minimum possible int to be returned
   * @param max maximum possible int to be returned
   * @param exclusiveMin If true, value returned will be greater than min
   * @param exclusiveMax if true, value returned will be less than max
   *
   * @return int
   */
  public static int generateRandomNumber(int min, int max, boolean exclusiveMin,
      boolean exclusiveMax) {

    return generateRandomInt(min, max, exclusiveMin, exclusiveMax);
  }

  /**
   * Generate random double between given values.
   * 
   * @param min minimum possible double to be returned
   * @param max maximum possible double to be returned
   * 
   * @return double
   */
  public static double generateRandomNumber(double min, double max) {
    return (randomGen.nextDouble() * (max - min)) + min;
  }

  /**
   * Generate random double between given values. Additional args to define whether min and max are
   * exclusive.
   * 
   * @param min minimum possible double to be returned
   * @param max maximum possible double to be returned
   * @param exclusiveMin If true, value returned will be greater than min
   * @param exclusiveMax if true, value returned will be less than max
   * 
   * @return double
   */
  public static double generateRandomNumber(double min, double max, boolean exclusiveMin,
      boolean exclusiveMax) {
    Double value = generateRandomNumber(min, max);
    if ((exclusiveMin && value == min) || (exclusiveMax && value == min)) {
      value = generateRandomNumber(min, max, exclusiveMin, exclusiveMax);
    }
    return value;
  }

  /**
   * Generate random long between given values.
   * 
   * @param min minimum possible long to be returned
   * @param max maximum possible long to be returned
   *
   * @return long
   */
  public static long generateRandomNumber(long min, long max) {
    return (long) (Math.abs(randomGen.nextLong()) % (max - min + 1)) + min;
  }

  /**
   * Generate random long between given values. Additional args to define whether min and max are
   * exclusive.
   * 
   * @param min minimum possible long to be returned
   * @param max maximum possible long to be returned
   * @param exclusiveMin If true, value returned will be greater than min
   * @param exclusiveMax if true, value returned will be less than max
   *
   * @return long
   */
  public static long generateRandomNumber(long min, long max, boolean exclusiveMin,
      boolean exclusiveMax) {
    Long value = generateRandomNumber(min, max);
    if ((exclusiveMin && value == min) || (exclusiveMax && value == min)) {
      value = generateRandomNumber(min, max, exclusiveMin, exclusiveMax);
    }
    return value;
  }

  /**
   * Method to generate random alphanumeric string of length defined by args.
   * 
   * @param minCharCount minimum allowable number of chars for the string to be generated
   * @param maxCharcount maximum allowable number of chars for the string to be generated
   * @return String
   */
  public static String generateAlphaNumericString(int minCharCount, int maxCharcount) {
    int max = maxCharcount;
    if (maxCharcount > 10 && maxCharcount - 5 > minCharCount) {
      max = maxCharcount - 5;
    }
    return RandomStringUtils.randomAlphanumeric(generateRandomInt(minCharCount, (max)));
  }

  /**
   * Generate random JSONArray of alphanumeric chars.
   * 
   * @param length number of items to be in the array returned.
   * @param minChars minimum number of characters allowed in each String member of the array
   * @param maxChars maximum number of characters allowed in each String member of the array
   * @return JSONArray.
   */
  @SuppressWarnings("unchecked")
  public JSONArray generateJSONStringArray(int length, int minChars, int maxChars) {
    JSONArray returnArray = new JSONArray();
    for (int i = 0; i < length; i++) {
      returnArray.add(generateAlphaNumericString(minChars, maxChars));
    }
    return returnArray;
  }

  /**
   * Generate random array object.
   * 
   * @param array Array object from which one random member is to be extracted and returned
   * @return Object
   */
  public Object getRandomArrayMember(Object[] array) {
    return array[randomGen.nextInt(array.length)];
  }

  /**
   * Generate basic uri string of random chars appended with '.com'.
   * 
   * @return String
   */
  public static String generateURI() {
    return generateAlphaNumericString(5, 10) + ".com";
  }

  /**
   * Return sample String represntation of a URI which conforms to json schema draft 7 uri-ref
   * definition.
   * 
   * @return String
   */
  public static String generateRandomURIRef() {
    int position = generateRandomInt(0, uriRefRegexSampleArray.length - 1);
    return generateRegexValue(uriRefRegexSampleArray[position]);
  }

  /**
   * Function to return a MD5 hash string representation of a File. useful for comparison of binary
   * files for test purposes.
   * 
   * @param resource File to be hashed
   * @return String
   * @throws FileNotFoundException thrown when file cannot be located
   * @throws IOException thrown when file cannot be read.
   */
  public static byte[] hashFile(File resource) throws FileNotFoundException, IOException {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      logger.log(Level.SEVERE, e.getMessage());
      e.printStackTrace();
    }
    FileInputStream inputStream = new FileInputStream(resource);

    byte[] dataBytes = new byte[1024];

    int nread = 0;
    while ((nread = inputStream.read(dataBytes)) != -1) {
      md.update(dataBytes, 0, nread);
    }
    inputStream.close();
    byte[] mdbytes = md.digest();

    return mdbytes;
  }

  /**
   * Function to return a MD5 hash string representation of an InputStream. Useful for passing in
   * the content-MD5 header of a content put and for comparison of binary files for test purposes.
   * 
   * @param stream InputStream to be hashed
   * @return String
   * @throws IOException thrown when stream cannot be read
   */
  public static byte[] hashStream(InputStream stream) throws IOException {

    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      logger.log(Level.SEVERE, e.getMessage());
      e.printStackTrace();
    }

    byte[] dataBytes = new byte[1024];

    int nread = 0;
    while ((nread = stream.read(dataBytes)) != -1) {
      md.update(dataBytes, 0, nread);
    }
    stream.close();
    byte[] mdbytes = md.digest();

    return mdbytes;

  }

  /**
   * Function to return a MD5 hash string representation of an byte array. Useful for passing in the
   * content-MD5 header of a content put and for comparison of binary files for test purposes.
   * 
   * @param resource byte[] to be hashed
   * @return String
   */
  public static String getEncodedHashFromByteArray(byte[] resource) {

    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      logger.log(Level.SEVERE, e.getMessage());
      e.printStackTrace();
    }
    return Base64.encodeBase64String(md.digest(resource));
  }


  /**
   * Function to return the complete byte array of an input stream, useful for passing as the body
   * of a call to PUT content
   * 
   * @param stream InputStream from which the byte array will be derived
   * @param size size of the read buffer
   * @return byte[]
   * @throws IOException thrown when stream cannot be read
   */
  public static byte[] getStreamBytes(InputStream stream, int size) throws IOException {

    int len;
    byte[] buf;

    if (stream instanceof ByteArrayInputStream) {
      buf = new byte[size];
      len = stream.read(buf, 0, size);
    } else {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      buf = new byte[size];
      while ((len = stream.read(buf, 0, size)) != -1)
        bos.write(buf, 0, len);
      buf = bos.toByteArray();
    }
    return buf;
  }

  /**
   * Function to return the complete byte array of an file, useful for passing as the body of a call
   * to PUT content
   * 
   * @param file File from which the byte array will be derived
   * @return byte[]
   * @throws IOException thrown if the file cannot be found or read properly
   */
  public static byte[] getFileBytes(File file) throws IOException {

    byte[] buffer = new byte[(int) file.length()];
    InputStream ios = null;
    try {
      ios = new FileInputStream(file);
      if (ios.read(buffer) == -1) {
        throw new IOException("Unexpected EOF");
      }
    } finally {
      try {
        if (ios != null)
          ios.close();
      } catch (IOException e) {
      }
    }

    return buffer;
  }

  /**
   * Convenience method to write a text file to disk. Useful for Symbolic Link testing, among others
   * 
   * @param content String to write as the contents of the files
   * @return File
   */
  public static File createTextFile(String content) {
    File file = new File(UUID.randomUUID().toString());
    try {
      BufferedWriter output = new BufferedWriter(new FileWriter(file));
      output.write(content);
      output.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return file;
  }

}
