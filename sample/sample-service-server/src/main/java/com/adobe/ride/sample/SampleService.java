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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

/**
 * 
 * NOTE: THIS IS SAMPLE CODE ONLY. Actual servers need much more robust checking of object creation,
 * sanitization of input, etc., etc.
 *
 * @author tedcasey
 * 
 */
@Path("/")
public class SampleService {
  private static final Logger logger = Logger.getLogger(SampleService.class.getName());
  public final static String OBJECT_NAME_REGEX = "[0-9a-zA-Z]{1}[0-9a-zA-Z_ %.-]{0,63}";
  public final static String OBJECT_TYPE_PATH_PARAM = "/{objectType}";
  public final static String OBJECT_NAME_PATH_PARAM = "/{objectName}";

  private static final String default_location = "/schemas/SampleService/type.json";

  JSONParser parser = new JSONParser();
  @Context
  ServletContext context;

  private boolean testTableExists = false;

  @PUT
  @Path("{objectType}/{objectName}")
  @Consumes("application/json")
  @Produces("application/json")
  @SuppressWarnings("unchecked")
  public Response createOrUpdateObject(String objectData,
      @PathParam("objectType") String objectType, @PathParam("objectName") String objectName)
      throws ParseException, IOException, ProcessingException, SQLException {
    String resource = default_location.replace("type", objectType);

    JsonNode schemaNode = JsonLoader.fromResource(resource);
    JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
    JsonSchema schema = factory.getJsonSchema(schemaNode);

    JsonNode objectMetadata = JsonLoader.fromString(objectData);
    ProcessingReport report = schema.validate(objectMetadata);

    String output = "";

    String responseString = (report.isSuccess()) ? "valid" : "invalid";
    Iterator<ProcessingMessage> reportIterator = report.iterator();

    JSONArray failureNodes = new JSONArray();

    int responseCode;

    if (responseString != "valid") {
      boolean failed = false;
      while (reportIterator.hasNext()) {
        ProcessingMessage msg = (ProcessingMessage) reportIterator.next();
        JsonNode failureObj = msg.asJson();
        String failureMessage = msg.asJson().get("message").toString();
        String lvl = failureObj.get("level").asText();
        // lvl = LogLevel.valueOf(lvl);
        if (lvl == "warning") {
          logger.log(Level.INFO, failureMessage);
        } else {
          failed = true;
          String failedNode = "unknown";
          if (msg.asJson().has("instance")) {
            failedNode = msg.asJson().get("instance").get("pointer").toString();
          }

          JSONObject failureNode = new JSONObject();
          failureNode.put("failedNode", failedNode);
          failureNode.put("failureMessage", failureMessage);
          failureNodes.add(failureNode);
        }
      }

      if (failed) {
        JSONObject response = new JSONObject();
        response.put("failures", failureNodes);
        response.put("message", "You have submitted an improperly formatted request");
        output = response.toJSONString();
        responseCode = 400;
      } else {
        Connection connection = connectToSQLiteDB();
        output = objectData;
        if (!testTableExists) {
          createRootTable(connection);
        }
        responseCode = updateOrCreateObject(connection, objectName, objectMetadata);
        connection.close();
      }
    } else {
      Connection connection = connectToSQLiteDB();
      output = objectData;
      if (!testTableExists) {
        createRootTable(connection);
      }
      responseCode = updateOrCreateObject(connection, objectName, objectMetadata);
      connection.close();
    }
    return Response.status(responseCode).entity(output).build();
  }

  @GET
  @Path("{objectType}/{objectName}")
  @Produces("application/json")
  public Response returnObject(String objectData, @PathParam("objectType") String objectType,
      @PathParam("objectName") String objectName) {
    System.out.println("GET Called @" + new Date().toString() + "\n");
    Connection connection = connectToSQLiteDB();
    String getObjectQuery = "SELECT id FROM object1Table where id='" + objectName + "';";
    ResultSet result = performGetAction(connection, getObjectQuery);
    try {
      connection.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return Response.status(200).entity(result).build();
  }

  @DELETE
  @Consumes("application/json")
  @Produces("application/json")
  @Path("{objectType}/{objectName}")
  public Response deleteObject(String objectData, @PathParam("objectType") String objectType,
      @PathParam("objectName") String objectName) throws SQLException {
    System.out.println("DELETE Called @" + new Date().toString() + "\n");
    Connection connection = connectToSQLiteDB();
    String getObjectQuery = "DELETE FROM object1Table WHERE id='" + objectName + "';";
    performSQLAction(connection, getObjectQuery);
    connection.close();

    System.out.println("Delete Called @" + new Date().toString() + "\n");
    return Response.status(204).build();
  }

  private int updateOrCreateObject(Connection connection, String objectName,
      JsonNode objectMetadata) throws SQLException {
    int count = 0;
    int responseCode = 500;

    String getObjectQuery = "SELECT id FROM object1Table where id='" + objectName + "';";
    ResultSet result = performGetAction(connection, getObjectQuery);

    try {
      while (result.next()) {
        count++;

      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    String query;

    if (count == 0) {
      query = "INSERT INTO object1Table (id, name, objectType, objectCode, thisProperty) "
          + "values (" + "'" + objectName + "', " + "'" + objectMetadata.get("name") + "', " + "'"
          + objectMetadata.get("objectType") + "'," + objectMetadata.get("objectCode") + ", " + "'"
          + objectMetadata.get("thisProperty") + "');";
      performSQLAction(connection, query);
      responseCode = 201;
    } else if (count == 1) {
      query = "UPDATE object1Table set name='" + objectMetadata.get("name") + "', " + "objectType='"
          + objectMetadata.get("objectType") + "', " + "objectCode="
          + objectMetadata.get("objectCode") + ", " + "thisProperty='"
          + objectMetadata.get("thisProperty") + "'" + " WHERE id='" + objectName + "';";
      performSQLAction(connection, query);
      connection.close();
      responseCode = 200;
    } else {
      System.out.println("Sample database error");
    }

    return responseCode;
  }

  private Connection connectToSQLiteDB() {
    Connection DBConnection = null;
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    try {
      // create a database connection
      // String debug_read = context.getRealPath("database/sample.db");
      DBConnection =
          DriverManager.getConnection("jdbc:sqlite:" + context.getRealPath("database/sample.db"));
    } catch (SQLException e) {
      // if the error message is "out of memory",
      // it probably means no database file is found
      System.err.println(e.getMessage());
    }

    return DBConnection;
  }

  @SuppressWarnings("unused")
  private boolean createDBFile() {
    boolean writeSuccess = false;
    try {
      Writer writer = new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream("database/sample.db"), "utf-8"));
      writer.close();
      writeSuccess = true;
    } catch (UnsupportedEncodingException | FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return writeSuccess;
  }

  private void createRootTable(Connection connection) {
    // Have to do this because, on deploy, the database is slammed everytime. In the real world, the
    // db most likely would not be located inside the server proj.
    String checkTableOrCreateQuery =
        "CREATE TABLE IF NOT EXISTS object1Table (id varchar(100), type varchar(100), name varchar(100), objectCode int(10), thisProperty varchar(100));";
    try {
      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30); // set timeout to 30 sec.
      statement.executeUpdate(checkTableOrCreateQuery);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    testTableExists = true;
  }

  private void performSQLAction(Connection connection, String query) {
    try {
      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30); // set timeout to 30 sec.
      statement.executeUpdate(query);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private ResultSet performGetAction(Connection connection, String query) {
    ResultSet result = null;
    try {
      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30); // set timeout to 30 sec.
      result = statement.executeQuery(query);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return result;
  }
}
