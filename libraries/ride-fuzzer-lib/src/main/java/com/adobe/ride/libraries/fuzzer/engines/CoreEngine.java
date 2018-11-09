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

package com.adobe.ride.libraries.fuzzer.engines;

import java.io.IOException;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;

import com.adobe.ride.core.RideCore;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;

/**
 * Core Fuzzing Engine, used by all sub-class engines for it's data providers and common methods and
 * properties.
 * 
 * @author tedcasey
 *
 */
public class CoreEngine extends RideCore {
  String encoding = "UTF-8";
  public String errorSchema;
  private ResponseSpecBuilder responseBuilder = new ResponseSpecBuilder();
  public ResponseSpecification noResponse;
  public static final String FUZZER_CLIENT_ID = "Pb-int-CP-fuzz";

  /** Fuzz objects passed into some of the DP arrays **/
  private Object testObj = new Object();
  private Object testString = "Test Test Test";
  private char[] testCharArray = new char[] {'T', 'E', 'S', 'T'};
  private double testDouble = Double.parseDouble("78547893.7583758349753489758934");
  private Number testNumber = 1456.34;
  private Byte testByte = 1;

  /**
   * Constructor for the core engine which initializes key members for subclasses.
   * 
   * @param scope String used to specify the scope of the testing for reporting (for example
   *        "article" in the metadata fuzzer).
   * @param target Target field to be fuzzed (for example a part of of a path or a metadata
   *        property).
   * @throws IOException
   */
  public CoreEngine(String scope, String target) {
    try {
      errorSchema = IOUtils
          .toString(this.getClass().getResourceAsStream("/serviceSchemas/error.json"), encoding);
    } catch (IOException e) {
      System.out.println("FUZZER ERROR - IOException: " + e.getMessage());
      e.printStackTrace();
    }
    noResponse = responseBuilder.build();

    // Prep dataproviders with fuzz data from the core and scope/target data from the subclass
    nonStringsFuzzValues = prepDP(nonStringsArray, scope, target);
    localizedStringsFuzzValues = prepDP(localizedStringsArray, scope, target);
    passiveSQLInjectionFuzzValues = prepDP(passiveSQLInjectionStringsArray, scope, target);
    noSQLInjectionFuzzValues = prepDP(noSQLInjectionStringsArray, scope, target);
    nonBooleanFuzzValues = prepDP(nonBooleanArray, scope, target);
  }

  /**
   * Method to validate a 4xx error responses for expected failures and 2xx expected successes.
   * 
   * @param response
   */
  public void validateResult(String property, Object testedValue, Response response,
      boolean expectSuccess) {
    int code = response.getStatusCode();

    if (expectSuccess == true && code == 400) {
      logger.log(Level.SEVERE, "Fuzzer Failure: \n   Property: " + property + "\n   TestedValue: "
          + testedValue.toString() + "\n   Expected: 2xx\n   Returned: 400");
    } else if (expectSuccess == false && (code == 200 || code == 201)) {
      logger.log(Level.SEVERE, "Fuzzer Failure: \n   Property: " + property + "\n   TestedValue: "
          + testedValue.toString() + "\n   Expected: 400\n   Returned: 2xx");
    }

    if (expectSuccess) {
      Assert.assertTrue(199 < code && code < 299);
    } else {
      Assert.assertEquals(code, 400);
    }
  }

  /**
   * Method to combine data for use in the dataProvider for reporting.
   * 
   * @param valuesArray Fuzz values to be added to the DP
   * @param scope Scope of the fuzz tests (i.e. "article" for MetadataFuzzer enabled tests).
   * @param target Field to be fuzzed (i.e. a metadata node in the MetadataFuzzer tests).
   * @return
   */
  private Object[][] prepDP(Object[] valuesArray, String scope, String target) {
    Object[][] dpArray = new Object[valuesArray.length][3];
    for (int i = 0; i < valuesArray.length; i++) {
      dpArray[i][0] = scope;
      dpArray[i][1] = target;
      dpArray[i][2] = valuesArray[i];
    }
    return dpArray;
  }

  /**
   * mutable Fuzz Value arrays to be returned by dataprovider methods below.
   */
  private Object[][] nonStringsFuzzValues;
  private Object[][] localizedStringsFuzzValues;
  private Object[][] passiveSQLInjectionFuzzValues;
  private Object[][] noSQLInjectionFuzzValues;
  private Object[][] nonBooleanFuzzValues;

  /**
   * Methods to provide dataproviders for Fuzz test methods.
   */

  @DataProvider(name = "nonStringsDP")
  public Object[][] nonStringsDataProvider() {
    return nonStringsFuzzValues;
  }

  @DataProvider(name = "localizedStringsDP")
  public Object[][] localizedStringsDataProvider() {
    return localizedStringsFuzzValues;
  }

  @DataProvider(name = "passiveSqlDP")
  public Object[][] passiveSQLInjectionDataProvider() {
    return passiveSQLInjectionFuzzValues;
  }

  @DataProvider(name = "noSqlDP")
  public Object[][] noSQLInjectionDataProvider() {
    return noSQLInjectionFuzzValues;
  }

  @DataProvider(name = "nonBooleanDP")
  public Object[][] nonBooleanDataProvider() {
    return nonBooleanFuzzValues;
  }

  /**
   * Arrays with actual fuzz values to be injected
   */

  // @formatter:off
  private Object[] nonStringsArray = {
    true,
    // testObj,
    -53,
    53,
    // testLong,
    testDouble,
    testNumber,
    // testCharArray,
    testByte,
    // testByteArray
  };

  @SuppressWarnings("unused")
  private Object[] problemCharsArray = {
    "///", // invalid location
    ":::", // improperly placed chars
    "#%&*{}<>?/+|" // illegal chars
  };

  private Object[] noSQLInjectionStringsArray = {
    "0;var date=new Date(); do{curDate = new Date();}while(curDate-date<10000)",
    "$where: function() { for (i = 0; i < 5; i++) {text += 'Failure Loop ' + i + '<br>';} }"
  };

  private Object[] passiveSQLInjectionStringsArray = {
    // OWASP recommended Values
    "1; select * from *.*;",
    "'test'; select * from *.*;",
    "'; create table pwned_by_ride (sanitize varchar(20), yo varchar(20), input varchar(20));",
    "0;var date=new Date(); do{curDate = new Date();}while(curDate-date<10000)",
    "' SELECT name FROM syscolumns WHERE id = (SELECT id FROM sysobjects WHERE name = tablename')--",
    "%27+OR+%277659%27%3D%277659",
    "1;(load_file(char(47,101,116,99,47,112,97,115,115,119,100))),1,1,1;",
    "'; exec master..xp_cmdshell",
    // "UNI/**/ON SEL/**/ECT", TODO: Server FGE fails to detect. Disagrees with Sample expectation. Need to investigate. Could be dangerous.
    "'/**/OR/**/1/**/=/**/1",
    // comment here to reduce OWASP set
    "'||(elt(-3+5,bin(15),ord(10),hex(char(45))))",
    "||6",
    "'||'6",
    "(||6)",
    "' OR 1=1-- ",
    "OR 1=1",
    "' OR '1'='1",
    "; OR '1'='1'",
    "%22+or+isnull%281%2F0%29+%2F*",
    "%27+OR+%277659%27%3D%277659",
    "%22+or+isnull%281%2F0%29+%2F*",
    "%27+--+",
    "' or 1=1--",
    " or 1=1--",
    "' or 1=1 /*",
    "or 1=1--",
    "' or 'a'='a",
    "') or ('a'='a",
    // "Admin' OR '" , TODO: Server FGE fails to detect. Should return a 400. Need to investigate. Could be dangerous.
    ") UNION SELECT%20*%20FROM%20INFORMATION_SCHEMA.TABLES;",
    "' having 1=1--",
    "' having 1=1--",
    "' group by userid having 1=1--",
    "' or 1 in (select @@version)--",
    "' union all select @@version--",
    "' OR 'unusual' = 'unusual'",
    "' OR 'something' = 'some'+'thing'",
    "' OR 'text' = N'text'",
    "' OR 'something' like 'some%'",
    "' OR 2 > 1",
    "' OR 'text' > 't'",
    "' OR 'whatever' in ('whatever')",
    "' OR 2 BETWEEN 1 and 3",
    "' or username like char(37);",
    "' union select * from users where login = char(114,111,111,116);",
    "' union select ",
    "'; EXECUTE IMMEDIATE 'SEL' || 'ECT US' || 'ER'",
    "'; EXEC ('SEL' + 'ECT US' + 'ER')",
    "' or 1/*",
    "+or+isnull%281%2F0%29+%2F*",
    "%22+or+isnull%281%2F0%29+%2F*",
    "%27+--+&password=",
    "'; begin declare @var varchar(8000) set @var=':' select @var=@var+'+login+'/'+password+' ' from users where login > ",
    "@var select @var as var into temp end --",
    "' and 1 in (select var from temp)--",
    "' union select 1,load_file('/etc/passwd'),1,1,1;",
    "' and 1=( if((load_file(char(110,46,101,120,116))<>char(39,39)),1,0));",
    // Google recommended passive SQL injection
    // "\\x27\\x4F\\x52 SELECT *" , TODO: Server FGE fails to detect. Disagrees with Sample expectation. Need to investigate. Could be dangerous.
    // "\\x27\\x6F\\x72 SELECT *" , TODO: ^^
    "'%20or%20'x'='x",
    "\"%20or%20\"x\"=\"x",
    // Comment here to reduce google set
    "'",
    "\"",
    "#",
    "-",
    "--",
    "'%20--",
    "--';",
    "'%20;",
    "=%20'",
    "=%20;",
    "=%20--",
    // "\\x23" , TODO: Server FGE fails to detect. Disagrees with Sample expectation. Need to investigate. Could be dangerous.
    // "\\x27" , TODO: ^^
    // "\\x3D%20\\x3B'" , TODO: ^^
    // "\\x3D%20\\x27" , TODO: ^^
    "'or%20select *",
    // "admin'--" , TODO: ^^
    "<>\"'%;)(&+",
    "'%20or%20''='",
    "'%20or%20'x'='x",
    "\"%20or%20\"x\"=\"x",
    "')%20or%20('x'='x",
    "0 or 1=1",
    "' or 0=0 --",
    "\" or 0=0 --",
    "or 0=0 --",
    "' or 0=0 #",
    "\" or 0=0 #",
    "or 0=0 #",
    "' or 1=1--",
    "\" or 1=1--",
    "' or '1'='1'--",
    "\"' or 1 --'\"",
    "or 1=1--",
    "or%201=1",
    "or%201=1 --",
    "' or 1=1 or ''='",
    "\" or 1=1 or \"\"=\"",
    "' or a=a--",
    "\" or \"a\"=\"a",
    "') or ('a'='a", "\") or (\"a\"=\"a",
    "hi\" or \"a\"=\"a",
    "hi\" or 1=1 --",
    "hi' or 1=1 --",
    "hi' or 'a'='a",
    "hi') or ('a'='a",
    "hi\") or (\"a\"=\"a",
    "'hi' or 'x'='x';",
    "@variable",
    ",@variable",
    // "PRINT" , TODO: Server FGE fails to detect. Disagrees with Sample expectation. Need to investigate. Could be dangerous.
    // "PRINT @@variable" , TODO: ^^
    // "select" , TODO: ^^
    // "insert" , TODO: ^^
    "as",
    "or",
    /*
     * TODO: Server FGE fails to detect. Disagrees with Sample expectation. Need to investigate. Could be dangerous. "procedure" , "limit" ,
     * "order by" , "asc" , "desc" , "delete" , "update" , "distinct" , "having" , "truncate" , "replace" , "like" , "handler" , "bfilename" ,
     */
    "' or username like '%",
    "' or uname like '%",
    "' or userid like '%",
    "' or uid like '%",
    "' or user like '%",
    // "exec xp" , TODO: Server FGE fails to detect. Disagrees with Sample expectation. Need to investigate. Could be dangerous.
    // "exec sp" , TODO: ^^
    "'; exec xp_regread",
    "t'exec master..xp_cmdshell 'nslookup www.google.com'--",
    "--sp_password"
  };
  
  @SuppressWarnings("unused")
  private Object[] nonIntArray = {
    testString,
    true,
    testObj,
    // testLong,
    testDouble,
    testNumber,
    testCharArray,
    testByte,
    // testByteArray
  };
  
  private Object[] nonBooleanArray = {
    testString,
    -53,
    53,
    testObj,
    // testLong,
    testDouble,
    testNumber,
    testCharArray,
    testByte,
    // testByteArray
  };
  
  private Object[] localizedStringsArray = {
    // reduced set of localized strings
    "あじえうぃ", // Hiragana
    "ｈｅｌｌｏ", // Full-width romaji
    "اعنتعزر نهتاثس", // Arabic
    "καλι μερα", // Greek
    "בישךךשי", // Hebrew
    "虎劲 挖池内", // Pinyin Simplified
    "öäåÖÄÅ", // High-ascii chars
    "フジウイイベ", // Katakana
    "ﾌｼﾞｲｳﾞｧ", // Half-width katakana
    "エヌヂオアバ", // Ainu
    "اعثمهتسةث", // Arabic-PC
    "沁 秋", // Wubi Xing
    "ㄊㄧ ㄕ ㄙㄠ", // Zhuyin
    "門佃" // Cangjie
  };
  // @formatter:on
}
