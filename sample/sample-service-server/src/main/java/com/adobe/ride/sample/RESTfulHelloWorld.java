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

package com.adobe.ride.sample;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
// import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * 
 * @author tedcasey
 *
 */
@Path("/hello")
public class RESTfulHelloWorld {
  @GET
  @Produces("text/html")
  public Response getStartingPage() {
    String output = "<h1>Hello World!<h1>" + "<p>RESTful Service is running ... <br>Ping @ "
        + new Date().toString() + "</p<br>";
    return Response.status(200).entity(output).build();
  }
}
