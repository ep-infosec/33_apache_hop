package org.apache.hop.metadata.api;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This class annotation signals to Hop Metadata that this object can be serialized. It also
 * provides information on how the object should be instantiated.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HopMetadataObject {

  Class<? extends IHopMetadataObjectFactory> objectFactory() default
      HopMetadataDefaultObjectFactory.class;

  /**
   * @return The field which is the key for this object. Hop will use this key to create the
   *     appropriate class instance of type with the provided object factory.
   */
  String xmlKey() default "";
}
