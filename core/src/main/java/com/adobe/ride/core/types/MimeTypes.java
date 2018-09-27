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

package com.adobe.ride.core.types;

/**
 * An enumeration of common mime types.
 * 
 * @author jpettit
 *
 */
public enum MimeTypes {
  JPEG("image/jpeg"),
  PNG("image/png"),
  BMP("image/bmp"),
  TIFF("image/tiff"),
  MP4("video/mp4"),
  MPEG("video/mpeg"),
  APP_JSON("application/JSON"),
  APP_ZIP("application/ZIP"),
  ARTICLE_ZIP("application/vnd.adobe.article+zip"),
  FOLIO_ZIP("application/vnd.adobe.folio+zip"),
  SYMBOL_LINK("application/vnd.adobe.symboliclink+json"),
  TTF("application/x-font-ttf"),
  OTF("application/x-font-opentype"),
  WOFF("application/x-font-woff");

  private final String value;

  private MimeTypes(String type) {
    this.value = type;
  }

  public String toString() {
    return this.value;
  }
}
