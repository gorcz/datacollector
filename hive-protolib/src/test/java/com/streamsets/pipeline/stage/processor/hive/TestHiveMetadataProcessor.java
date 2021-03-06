/**
 * Copyright 2015 StreamSets Inc.
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
package com.streamsets.pipeline.stage.processor.hive;

import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.sdk.RecordCreator;
import com.streamsets.pipeline.lib.util.SdcAvroTestUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestHiveMetadataProcessor {

  @Test
  public void testRecordHeaderToHDFSRoll() throws Exception {
    Record record = RecordCreator.create();
    String sample = "sample record";
    record.set(Field.create(sample));
    String targetDir = "/user/hive/warehouse/table";
    String samplePartition = "/dt=2016-05-24/country=US";

    HiveMetadataProcessor.updateRecordForHDFS(record, true, SdcAvroTestUtil.AVRO_SCHEMA, targetDir, samplePartition);
    Assert.assertEquals(record.getHeader().getAttribute(HiveMetadataProcessor.HDFS_HEADER_ROLL), "true");
    Assert.assertEquals(record.getHeader().getAttribute(
        HiveMetadataProcessor.HDFS_HEADER_AVROSCHEMA),
        SdcAvroTestUtil.AVRO_SCHEMA);
    Assert.assertEquals(record.getHeader().getAttribute(
        HiveMetadataProcessor.HDFS_HEADER_TARGET_DIRECTORY),
        (targetDir+samplePartition));
  }

  @Test
  public void testRecordHeaderToHDFSNoRoll() throws Exception {
    Record record = RecordCreator.create();
    String sample = "sample record";
    record.set(Field.create(sample));
    String targetDir = "/user/hive/warehouse/table";
    String samplePartition = "dt=2016/state=CA";


    HiveMetadataProcessor.updateRecordForHDFS(record, false, SdcAvroTestUtil.AVRO_SCHEMA, targetDir, samplePartition);
    Assert.assertNull(record.getHeader().getAttribute(HiveMetadataProcessor.HDFS_HEADER_ROLL));
    Assert.assertEquals(record.getHeader().getAttribute(
        HiveMetadataProcessor.HDFS_HEADER_AVROSCHEMA),
        SdcAvroTestUtil.AVRO_SCHEMA);
    Assert.assertEquals(record.getHeader().getAttribute(
        HiveMetadataProcessor.HDFS_HEADER_TARGET_DIRECTORY),
        (targetDir+samplePartition));

  }
}
