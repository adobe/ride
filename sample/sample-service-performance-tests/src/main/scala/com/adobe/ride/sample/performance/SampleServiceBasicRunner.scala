/*
Copyright 2018 Adobe. All rights reserved.
This file is licensed to you under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License. You may obtain a copy
of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
OF ANY KIND, either express or implied. See the License for the specific language
governing permissions and limitations under the License.
*/
/**
 * @author tedcasey
 */
package com.adobe.ride.sample.performance

import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder
import io.gatling.core.Predef._

import com.adobe.ride.core.types.ExpectedResponse

import com.adobe.ride.libraries.performance.Predef._
import com.adobe.ride.libraries.performance.RideServiceClient

import java.util.UUID

import com.adobe.ride.sample.cloud_objects.SampleServiceObject1
import com.adobe.ride.sample.core.SampleServiceController

object SampleServiceBasicRunner {

  def main(args: Array[String]) {
    val simClass = classOf[RideSim].getName

    val props = new GatlingPropertiesBuilder
    props.simulationClass(simClass)
    Gatling.fromMap(props.build)
  }

  class RideSim extends Simulation {
    val rideProtocol = ride.protocol()
    // (4) Change the name of the Sample Service client constructor here to match the class name you use on line 39 below
    val testClass = new SampleServiceClient()
    val scn = scenario(testClass.scenario).exec(ride(testClass).connect)
    setUp(
      scn.inject(atOnceUsers(testClass.connections))).protocols(rideProtocol)
  }
}

// (1) Change the name of the SampleClient class to whatever you are testing.  ForExample - ServiceThatImTestingClient extends RideServiceClient
class SampleServiceClient extends RideServiceClient {
  // (2) Set the name of the report and number of simulated users
  val scenario: String = "ZenActionsSimulation" //Name to use in Gatling reports
  val connections: Int = 5 //Number of concurrent users to run this performance test

  def run = {

    // (3) This is where your Ride test code should reside.  Replace all of the sample code below with your Ride code.
    // Note that this is Scala code, so you'll need to modify your normal Ride test code to look like the scala syntax illustrated below
    val itemName: String = UUID.randomUUID().toString()
    val testObject: SampleServiceObject1 = new SampleServiceObject1(itemName, false)

    SampleServiceController.createOrUpdateObject(testObject.getObjectPath(), testObject, ExpectedResponse.CREATED_RESPONSE, true)
    // ------
  }
}
