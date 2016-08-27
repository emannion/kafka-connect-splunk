/**
 * Copyright (C) ${project.inceptionYear} Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.confluent.kafka.connect.splunk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.http.HttpContent;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

class SinkRecordContent implements HttpContent {
  final Collection<SinkRecord> sinkRecords;
  final Cache<Schema, List<ValueWriter>> schemaCache = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .build();

  SinkRecordContent(Collection<SinkRecord> sinkRecords) {
    this.sinkRecords = sinkRecords;
  }

  @Override
  public long getLength() throws IOException {
    return -1;
  }

  @Override
  public String getType() {
    return null;
  }

  @Override
  public boolean retrySupported() {
    return false;
  }

  List<ValueWriter> getWriters(Schema schema) {
    List<ValueWriter> writers = new ArrayList<>();

    Map<String, Field> fieldLookup = new HashMap<>();
    for (Field field : schema.fields()) {
      fieldLookup.put(field.name(), field);
    }

    if (fieldLookup.containsKey("host")) {
      writers.add(new ValueWriter() {
        @Override
        public void write(ObjectNode objectNode, Struct struct) {
          String host = struct.getString("host");
          if (null != host) {
            objectNode.put("host", host);
          }
        }
      });
    }

    if (fieldLookup.containsKey("hostname")) {
      writers.add(new ValueWriter() {
        @Override
        public void write(ObjectNode objectNode, Struct struct) {
          String host = struct.getString("hostname");
          if (null != host) {
            objectNode.put("host", host);
          }
        }
      });
    }

    if (fieldLookup.containsKey("time")) {
      writers.add(new ValueWriter() {
        @Override
        public void write(ObjectNode objectNode, Struct struct) {
          Date dateTime = (Date) struct.get("time");
          if (null != dateTime) {
            BigDecimal time = new BigDecimal(BigInteger.valueOf(dateTime.getTime()), 3);
            objectNode.put("time", time);
          }
        }
      });
    }

    if (fieldLookup.containsKey("sourcetype")) {
      writers.add(new ValueWriter() {
        @Override
        public void write(ObjectNode objectNode, Struct struct) {
          String host = struct.getString("sourcetype");
          if (null != host) {
            objectNode.put("sourcetype", host);
          }
        }
      });
    }

    if (fieldLookup.containsKey("index")) {
      writers.add(new ValueWriter() {
        @Override
        public void write(ObjectNode objectNode, Struct struct) {
          String host = struct.getString("index");
          if (null != host) {
            objectNode.put("index", host);
          }
        }
      });
    }

    if (fieldLookup.containsKey("source")) {
      writers.add(new ValueWriter() {
        @Override
        public void write(ObjectNode objectNode, Struct struct) {
          String host = struct.getString("source");
          if (null != host) {
            objectNode.put("source", host);
          }
        }
      });
    }

    return writers;
  }

  @Override
  public void writeTo(OutputStream outputStream) throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    int index = 0;

    for (SinkRecord sinkRecord : this.sinkRecords) {
      if (index > 0) {
        outputStream.write((int) '\n');
      }

      final Struct valueStruct = (Struct) sinkRecord.value();
      List<ValueWriter> writers;
      try {
        writers = this.schemaCache.get(valueStruct.schema(), new Callable<List<ValueWriter>>() {
          @Override
          public List<ValueWriter> call() throws Exception {
            return getWriters(valueStruct.schema());
          }
        });
      } catch (ExecutionException e) {
        throw new IOException(e);
      }

      ObjectNode objectNode = mapper.createObjectNode();
      for (ValueWriter writer : writers) {
        writer.write(objectNode, valueStruct);
      }

      mapper.writeValue(outputStream, objectNode);
      index++;
    }

    outputStream.flush();
  }

  interface ValueWriter {
    void write(ObjectNode objectNode, Struct struct);
  }
}
