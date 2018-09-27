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
package com.adobe.ride.libraries.performance

import io.gatling.commons.stats.OK
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.protocol.ProtocolComponentsRegistry
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen

/**
 * @author: tedcasey
 */
class RideActionBuilder(testclass: RideServiceClient) extends ActionBuilder {
	private def components(protocolComponentsRegistry: ProtocolComponentsRegistry) =
		protocolComponentsRegistry.components(RideProtocol.RideProtocolKey)
		
	override def build(ctx: ScenarioContext, next: Action): Action = {
		import ctx._
		val statsEngine = coreComponents.statsEngine
		val rideComponents = components(protocolComponentsRegistry)
		new RideConnect(rideComponents.rideProtocol, statsEngine, next, testclass)
	}
}

/**
 * @author tedcasey
 */
class RideConnect(protocol: RideProtocol, val statsEngine: StatsEngine, val next: Action, testClass: RideServiceClient) extends ExitableAction with NameGen {
	override def name: String = genName("rideConnect")

	override def execute(session: Session) = {
		val start = System.currentTimeMillis
		testClass.run
		val end = System.currentTimeMillis
		val timings = ResponseTimings(start, end)
		statsEngine.logResponse(session, name, timings, OK, None, None)
		next ! session
	}
}
