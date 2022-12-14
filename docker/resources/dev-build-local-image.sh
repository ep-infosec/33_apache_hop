#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#

# USE THIS FOR LOCAL DEV ONLY
cd ../..

if [ "$1" != "no-mvn" ]
then
  echo "Building Apache Hop: use first argument 'no-mvn' to skip this part."

  # create new build
  mvn -DskipTests=true clean install

  # unzip new build
  unzip assemblies/client/target/hop-client-*.zip -d assemblies/client/target
fi

if [ "$2" != "latest" ]
then
  echo "Using the current time as build version. Use second argument 'latest' to use that tag."
  LATEST_BUILD_VERSION=`date '+%Y%m%d%H%M%S'`
else
  LATEST_BUILD_VERSION="latest"
fi

# build the docker image
docker build . -f docker/Dockerfile -t apache-hop:${LATEST_BUILD_VERSION}
