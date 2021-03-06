/**
 * Copyright © 2016 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder.kafka.connect.splunk;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class SplunkHttpSinkConnectorTest {
  @Test
  public void test() {
    SplunkHttpSinkConnector splunkHttpSinkConnector = new SplunkHttpSinkConnector();
    Map<String, String> settings = new LinkedHashMap<>();
    settings.put(SplunkHttpSinkConnectorConfig.REMOTE_HOST_CONF, "localhost");
    settings.put(SplunkHttpSinkConnectorConfig.AUTHORIZATION_TOKEN_CONF, "132412341234123");
    splunkHttpSinkConnector.start(settings);
    List<Map<String, String>> taskConfigs = splunkHttpSinkConnector.taskConfigs(10);
    assertNotNull(taskConfigs);
    assertEquals(10, taskConfigs.size());
  }
}