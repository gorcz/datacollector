/**
 * Copyright 2016 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.streamsets.datacollector.event.client.impl;

import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.filter.CsrfProtectionFilter;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.GZipEncoder;

import com.streamsets.datacollector.event.client.api.EventClient;
import com.streamsets.datacollector.event.client.api.EventException;
import com.streamsets.datacollector.event.json.ClientEventJson;
import com.streamsets.datacollector.event.json.ServerEventJson;

public class EventClientImpl implements EventClient {

  private final String targetURL;
  private final Client client;

  public EventClientImpl(String targetURL) {
    this.targetURL = targetURL;
    this.client = ClientBuilder.newClient();
    client.register(new CsrfProtectionFilter("CSRF"));
  }

  @Override
  public List<ServerEventJson> submit(
    String path,
    Map<String, String> queryParams,
    Map<String, String> headerParams,
    boolean compression,
    List<ClientEventJson> clientEventJson) throws EventException {

    if (compression) {
      client.register(GZipEncoder.class);
      client.register(EncodingFilter.class);
    }

    WebTarget target = client.target(targetURL + path);

    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
      target.queryParam(entry.getKey(), entry.getValue());
    }

    Invocation.Builder builder = target.request();

    for (Map.Entry<String, String> entry : headerParams.entrySet()) {
      builder = builder.header(entry.getKey(), removeNewLine(entry.getValue()));
    }

    Response response = builder.post(Entity.json(clientEventJson));
    if (response.getStatus() != 200) {
      throw new EventException("Failed : HTTP error code : " + response.getStatus());
    }
    try {
      return response.readEntity(new GenericType<List<ServerEventJson>>() {
      });
    } catch (Exception ex) {
      throw new EventException("Failed to read response : " + ex);
    }
  }

  public String removeNewLine(String original) {
    return original.replaceAll("(\\r|\\n)", "");
  }
}
