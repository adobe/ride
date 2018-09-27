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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * 
 * @author hook
 *
 */
public class FileUtils {
  public static String getUserHome() {
    String dir = System.getProperty("user.home");
    return dir;
  }

  public static String getUserDirectory() {
    return System.getProperty("user.dir");
  }

  public static void validate(String filePath) {
    File file = new File(filePath);
    validate(file);
  }

  @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH",
      justification = "This wasn't authored by QE and may be refactored at a later date.")
  public static void validate(File file) {
    if (file == null || !file.exists()) {
      throw new FileDoesNotExistException(
          String.format("file: %s does not exist", file.getAbsolutePath()));
    }
  }

  public static void closeQuietly(ILogUtils log, Closeable stream) {
    if (stream != null) {
      try {
        stream.close();
        stream = null;
      } catch (IOException e) {
        if (log != null)
          log.error(e);
      }
    }
  }

  public static void closeQuietly(Closeable stream) {
    if (stream != null) {
      try {
        stream.close();
        stream = null;
      } catch (IOException e) {
        // no logging context
      }
    }
  }

  public synchronized static void deleteFileQuietly(ILogUtils log, File file) {
    if (file != null && file.exists()) {
      try {
        file.delete();
      } catch (SecurityException e) {
        if (log != null)
          log.error(e);
      }
    }
  }

  public synchronized static void deleteFileQuietly(File file) {
    if (file != null && file.exists()) {
      try {
        file.delete();
      } catch (SecurityException e) {
        // no logging context
      }
    }
  }

  public synchronized static String readFileAsUTF8(String path)
      throws NoSuchFileException, IOException {
    Validation.isNullOrEmptyParameter("path", path);

    Charset encoding = StandardCharsets.UTF_8;

    return readFile(path, encoding);
  }

  public synchronized static String readFile(String path, Charset encoding)
      throws NoSuchFileException, IOException {
    Validation.isNullOrEmptyParameter("path", path);
    Validation.isNullOrEmptyParameter("encoding", encoding);

    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return encoding.decode(ByteBuffer.wrap(encoded)).toString();
  }

  public synchronized static String writeFileDataToStringAsUTF8(InputStream inputStream)
      throws IOException {
    Validation.isNullOrEmptyParameter("inputStream", inputStream);

    Charset encoding = StandardCharsets.UTF_8;

    String data = IOUtils.toString(inputStream, encoding);
    return data;
  }

  public synchronized static String writeFileDataToString(InputStream inputStream, Charset encoding)
      throws IOException {
    Validation.isNullOrEmptyParameter("inputStream", inputStream);
    Validation.isNullOrEmptyParameter("encoding", encoding);

    String data = IOUtils.toString(inputStream, encoding);
    return data;
  }
}
