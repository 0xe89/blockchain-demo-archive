#!/bin/bash
#
# Copyright IBM Corp All Rights Reserved
#
# SPDX-License-Identifier: Apache-2.0
echo "clean 3 orderer ......"
docker-compose -f ../basic-network/docker/docker-compose-3orderer.yaml down

echo "clean org1-2peer with couchdb ......"
docker-compose -f ../basic-network/docker/docker-compose-org1-2peer-couchdb.yaml down

echo "clean org2-2peer with couchdb ......"
docker-compose -f ../basic-network/docker/docker-compose-org2-2peer-couchdb.yaml down


