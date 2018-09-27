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

package com.adobe.ride.core.log;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.xml.XmlSuite;

/*
 * authors: stlarson, tedcasey
 */

public class TestNGCustomReporter implements IReporter {

  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
      String outputDirectory) {
    print("Suites run: " + suites.size());
    for (ISuite suite : suites) {
      print("Suite: " + suite.getName());
      Map<String, ISuiteResult> suiteResults = suite.getResults();
      for (String testName : suiteResults.keySet()) {
        print("Test:" + testName);
        ISuiteResult suiteResult = suiteResults.get(testName);
        ITestContext testContext = suiteResult.getTestContext();
        print("Failed:" + testContext.getFailedTests().size());
        IResultMap failedResult = testContext.getFailedTests();
        Set<ITestResult> testsFailed = failedResult.getAllResults();
        for (ITestResult testResult : testsFailed) {
          print("--------------------------------------");
          print("Failed Test: " + testResult.getName());
          print("Reason: " + testResult.getThrowable().getMessage());
          String sanitized =
              Reporter.getOutput(testResult).toString().replace("[", "").replace("]", "");
          print(sanitized);
        }
        IResultMap passResult = testContext.getPassedTests();
        Set<ITestResult> testsPassed = passResult.getAllResults();
        print("        Passed>" + testsPassed.size());
        for (ITestResult testResult : testsPassed) {
          print("            " + testResult.getName() + ">took "
              + (testResult.getEndMillis() - testResult.getStartMillis()) + "ms");
        }
        IResultMap skippedResult = testContext.getSkippedTests();
        Set<ITestResult> testsSkipped = skippedResult.getAllResults();
        print("        Skipped>" + testsSkipped.size());
        for (ITestResult testResult : testsSkipped) {
          print("            " + testResult.getName());
        }

      }
    }
  }

  private void print(String text) {
    System.out.println(text);
  }
}
