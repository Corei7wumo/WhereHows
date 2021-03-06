/**
 * Copyright 2015 LinkedIn Corp. All rights reserved.
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
 */
package utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.daos.DatasetInfoDao;
import org.apache.avro.generic.GenericData;
import wherehows.common.schemas.Record;
import wherehows.common.utils.StringUtil;


public class GobblinTrackingAuditProcessor{


  /**
   * Process a Gobblin tracking event audit record
   * @param record
   * @param topic
   * @throws Exception
   */

  final private static String DALI_LIMITED_RETENTION_AUDITOR = "DaliLimitedRetentionAuditor";
  final private static String DALI_AUTOPURGED_AUDITOR = "DaliAutoPurgeAuditor";
  final private static String DS_IGNORE_IDPC_AUDITOR = "DsIgnoreIDPCAuditor";
  final private static String DATASET_URN_PREFIX = "hdfs://";
  final private static String DATASET_OWNER_SOURCE = "IDPC";

  public Record process(GenericData.Record record, String topic) throws Exception {

    if (record != null) {
      String name = (String) record.get("name");
      // only handle "DaliLimitedRetentionAuditor","DaliAutoPurgeAuditor" and "DsIgnoreIDPCAuditor"
      if (name.equals(DALI_LIMITED_RETENTION_AUDITOR) ||
          name.equals(DALI_AUTOPURGED_AUDITOR) ||
          name.equals(DS_IGNORE_IDPC_AUDITOR))
      {
        Long timestamp = (Long) record.get("timestamp");
        Map<String, String> metadata = StringUtil.convertObjectMapToStringMap(record.get("metadata"));

        String hasError = metadata.get("HasError");
        if (!hasError.equalsIgnoreCase("true"))
        {
          String datasetUrn = metadata.get("DatasetPath");
          String ownerUrns = metadata.get("OwnerURNs");
          DatasetInfoDao.updateKafkaDatasetOwner(
                  DATASET_URN_PREFIX + datasetUrn,ownerUrns,
                  DATASET_OWNER_SOURCE,
                  timestamp);
        }
      }
    }
    return null;
  }
}
