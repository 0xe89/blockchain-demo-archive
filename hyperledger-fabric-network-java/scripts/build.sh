#!/bin/bash
#
# Copyright IBM Corp All Rights Reserved
#
# SPDX-License-Identifier: Apache-2.0
### build ###
echo "start 3 orderer ......"
docker-compose -f ../basic-network/docker/docker-compose-3orderer.yaml up -d

echo "start org1-2peer with couchdb ......"
docker-compose -f ../basic-network/docker/docker-compose-org1-2peer-couchdb.yaml up -d

echo "start org2-2peer with couchdb ......"
docker-compose -f ../basic-network/docker/docker-compose-org2-2peer-couchdb.yaml up -d

echo "Create channel ......"
docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" fabric-cli peer channel create -o orderer0.example.com:7050 -c "businesschannel" -f "/tmp/channel-artifacts/businesschannel.tx" --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/example.com/orderers/orderer0.example.com/msp/tlscacerts/tlsca.example.com-cert.pem
docker exec fabric-cli mv businesschannel.block /tmp/channel-artifacts/

### deploy ###
echo "Join the channel ......"
echo "org1-peer0 加入通道"
docker exec -e "CORE_PEER_TLS_ENABLED=true" -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" -e "CORE_PEER_ADDRESS=peer0.org1.example.com:7051" fabric-cli peer channel join -b /tmp/channel-artifacts/businesschannel.block

echo "org1-peer1 加入通道"
docker exec -e "CORE_PEER_TLS_ENABLED=true" -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/peers/peer1.org1.example.com/tls/ca.crt" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_ADDRESS=peer1.org1.example.com:8051" fabric-cli peer channel join -b /tmp/channel-artifacts/businesschannel.block

echo "org2-peer0 加入通道"
docker exec -e "CORE_PEER_TLS_ENABLED=true" -e "CORE_PEER_LOCALMSPID=Org2MSP" -e "CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org2.example.com:9051" fabric-cli peer channel join -b /tmp/channel-artifacts/businesschannel.block

echo "org2-peer1 加入通道"
docker exec -e "CORE_PEER_TLS_ENABLED=true" -e "CORE_PEER_LOCALMSPID=Org2MSP" -e "CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp" -e "CORE_PEER_ADDRESS=peer1.org2.example.com:10051" fabric-cli peer channel join -b /tmp/channel-artifacts/businesschannel.block

echo "Update anchor peer"
echo "org1 更新锚节点"
docker exec -e "CORE_PEER_TLS_ENABLED=true" -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org1.example.com:7051" fabric-cli peer channel update -o orderer0.example.com:7050 -c businesschannel -f /tmp/channel-artifacts/Org1MSPanchors.tx --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/example.com/orderers/orderer0.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

echo "org2 更新锚节点"
docker exec -e "CORE_PEER_TLS_ENABLED=true" -e "CORE_PEER_LOCALMSPID=Org2MSP" -e "CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org2.example.com:9051" fabric-cli peer channel update -o orderer0.example.com:7050 -c businesschannel -f /tmp/channel-artifacts/Org2MSPanchors.tx --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/example.com/orderers/orderer0.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

echo "Packing chaincode"
docker exec -e "CORE_PEER_TLS_ENABLED=true" -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" -e "CORE_PEER_ADDRESS=peer0.org1.example.com:7051" fabric-cli peer lifecycle chaincode package hyperledger-fabric-contract-java-demo.tar.gz --path /etc/hyperledger/fabric/chaincodes/hyperledger-fabric-contract-java-demo/ --lang java --label hyperledger-fabric-contract-java-demo_1

echo "Installation chain code"
echo "在 org1-peer0 节点上安装链码"
docker exec -e "CORE_PEER_TLS_ENABLED=true" -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org1.example.com:7051" fabric-cli peer lifecycle chaincode install hyperledger-fabric-contract-java-demo.tar.gz

echo "在 org1-peer1 节点上安装链码"
docker exec -e "CORE_PEER_TLS_ENABLED=true" -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/peers/peer1.org1.example.com/tls/ca.crt" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_ADDRESS=peer1.org1.example.com:8051" fabric-cli peer lifecycle chaincode install hyperledger-fabric-contract-java-demo.tar.gz

echo "在 org2-peer0 节点上安装链码"
docker exec -e "CORE_PEER_TLS_ENABLED=true" -e "CORE_PEER_LOCALMSPID=Org2MSP" -e "CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org2.example.com:9051" fabric-cli peer lifecycle chaincode install hyperledger-fabric-contract-java-demo.tar.gz

echo "在 org2-peer1 节点上安装链码"
docker exec -e "CORE_PEER_TLS_ENABLED=true" -e "CORE_PEER_LOCALMSPID=Org2MSP" -e "CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/peers/peer1.org2.example.com/tls/ca.crt" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp" -e "CORE_PEER_ADDRESS=peer1.org2.example.com:10051" fabric-cli peer lifecycle chaincode install hyperledger-fabric-contract-java-demo.tar.gz

printf "Please enter PACKAGE ID\n"
read CC_PACKAGE_ID

echo "批准链码定义"
echo "org1 批准链码定义"
docker exec -e "CC_PACKAGE_ID=$CC_PACKAGE_ID" -e "CORE_PEER_TLS_ENABLED=true" -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org1.example.com:7051" fabric-cli peer lifecycle chaincode approveformyorg -o orderer0.example.com:7050 --ordererTLSHostnameOverride orderer0.example.com --channelID businesschannel --name hyperledger-fabric-contract-java-demo --version 1.0 --package-id "$CC_PACKAGE_ID" --sequence 1 --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/example.com/orderers/orderer0.example.com/msp/tlscacerts/tlsca.example.com-cert.pem --collections-config /etc/hyperledger/fabric/chaincodes/hyperledger-fabric-contract-java-demo/collections_config.json --signature-policy "OR('Org1MSP.member','Org2MSP.member')"

echo "org2 批准链码定义"
docker exec -e "CC_PACKAGE_ID=$CC_PACKAGE_ID" -e "CORE_PEER_TLS_ENABLED=true" -e "CORE_PEER_LOCALMSPID=Org2MSP" -e "CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org2.example.com:9051" fabric-cli peer lifecycle chaincode approveformyorg -o orderer0.example.com:7050 --ordererTLSHostnameOverride orderer0.example.com --channelID businesschannel --name hyperledger-fabric-contract-java-demo --version 1.0 --package-id $CC_PACKAGE_ID --sequence 1 --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/example.com/orderers/orderer0.example.com/msp/tlscacerts/tlsca.example.com-cert.pem --collections-config /etc/hyperledger/fabric/chaincodes/hyperledger-fabric-contract-java-demo/collections_config.json --signature-policy "OR('Org1MSP.member','Org2MSP.member')"

echo "检查通道成员是否已批准相同的链码定义"
docker exec -e "CORE_PEER_TLS_ENABLED=true" -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" -e "CORE_PEER_ADDRESS=peer0.org1.example.com:7051" fabric-cli peer lifecycle chaincode checkcommitreadiness --channelID businesschannel --name hyperledger-fabric-contract-java-demo --version 1.0 --sequence 1 --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/example.com/orderers/orderer0.example.com/msp/tlscacerts/tlsca.example.com-cert.pem --output json --collections-config /etc/hyperledger/fabric/chaincodes/hyperledger-fabric-contract-java-demo/collections_config.json --signature-policy "OR('Org1MSP.member','Org2MSP.member')"


echo "将链码提交到通道"
docker exec -e "CORE_PEER_TLS_ENABLED=true" -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" -e "CORE_PEER_ADDRESS=peer0.org1.example.com:7051" fabric-cli peer lifecycle chaincode commit -o orderer0.example.com:7050 --ordererTLSHostnameOverride orderer0.example.com --channelID businesschannel --name hyperledger-fabric-contract-java-demo --version 1.0 --sequence 1 --tls --cafile /etc/hyperledger/fabric/crypto-config/ordererOrganizations/example.com/orderers/orderer0.example.com/msp/tlscacerts/tlsca.example.com-cert.pem --peerAddresses peer0.org1.example.com:7051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt --peerAddresses peer0.org2.example.com:9051 --tlsRootCertFiles /etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt --collections-config /etc/hyperledger/fabric/chaincodes/hyperledger-fabric-contract-java-demo/collections_config.json --signature-policy "OR('Org1MSP.member','Org2MSP.member')"
