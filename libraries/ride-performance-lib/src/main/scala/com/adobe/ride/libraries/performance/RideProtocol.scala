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

import akka.actor.ActorSystem
import io.gatling.core
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolComponents, ProtocolKey}
import io.gatling.core.session.Session

/**
 * @author tedcasey
 */
class RideProtocol() extends Protocol {
	type Components = RideComponents
}

object RideProtocol {
	def apply() = new RideProtocol()

	val RideProtocolKey = new ProtocolKey {

		type Protocol = RideProtocol
		type Components = RideComponents

		override def protocolClass: Class[core.protocol.Protocol] = classOf[RideProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

		override def defaultProtocolValue(configuration: GatlingConfiguration): RideProtocol = throw new IllegalStateException("Can't provide a default value for UpperProtocol")

		override def newComponents(system: ActorSystem, coreComponents: CoreComponents): RideProtocol => RideComponents = {
			rideProtocol => RideComponents(rideProtocol)
		}
	}
}

case class RideComponents(rideProtocol: RideProtocol) extends ProtocolComponents {
	def onStart: Option[Session => Session] = None

	def onExit: Option[Session => Unit] = None
}

case class RideProtocolBuilder() {
	def build() = RideProtocol()
}

object RideProtocolBuilder {
	def protocol() = RideProtocolBuilder()
}
