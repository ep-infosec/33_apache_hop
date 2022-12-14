/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.hop.pipeline.transforms.cassandraoutput;

import org.junit.Assert;
import org.junit.Test;

public class CassandraOutputMetaTest {
  @Test
  public void validateConvertToSecondsWithNONETTLUnits() {
    CassandraOutputMeta.TtlUnits ttlUnit = CassandraOutputMeta.TtlUnits.NONE;
    long value = 1;
    value = ttlUnit.convertToSeconds(value);

    Assert.assertEquals(-1, value);
  }

  @Test
  public void validateConvertToSecondsWithSecondsTTLUnits() {
    CassandraOutputMeta.TtlUnits ttlUnit = CassandraOutputMeta.TtlUnits.SECONDS;
    long value = 1;
    value = ttlUnit.convertToSeconds(value);

    Assert.assertEquals(1, value);
  }

  @Test
  public void validateConvertToSecondsWithMinutesTTLUnits() {
    CassandraOutputMeta.TtlUnits ttlUnit = CassandraOutputMeta.TtlUnits.MINUTES;
    long value = 1;
    value = ttlUnit.convertToSeconds(value);

    Assert.assertEquals(60, value);
  }

  @Test
  public void validateConvertToSecondsWithHOURSTTLUnits() {
    CassandraOutputMeta.TtlUnits ttlUnit = CassandraOutputMeta.TtlUnits.HOURS;
    long value = 1;
    value = ttlUnit.convertToSeconds(value);

    Assert.assertEquals(3600, value);
  }

  @Test
  public void validateConvertToSecondsWithDAYSTTLUnits() {
    CassandraOutputMeta.TtlUnits ttlUnit = CassandraOutputMeta.TtlUnits.DAYS;
    long value = 1;
    value = ttlUnit.convertToSeconds(value);

    Assert.assertEquals(86400, value);
  }
}
