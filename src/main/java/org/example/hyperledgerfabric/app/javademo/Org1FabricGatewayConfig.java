package org.example.hyperledgerfabric.app.javademo;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.CallOption;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.identity.Identities;
import org.hyperledger.fabric.client.identity.Signers;
import org.hyperledger.fabric.client.identity.X509Identity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
@Configuration
@Lazy
@AllArgsConstructor
@Slf4j
public class Org1FabricGatewayConfig {

    private HyperLedgerFabricProperties hyperLedgerFabricProperties;

    @Bean(name = "Org1Gateway")
    public Gateway gateway() throws Exception {
        hyperLedgerFabricProperties.setMspId("Org1MSP");
        hyperLedgerFabricProperties.setNetworkConnectionConfigPath("src/main/resources/org1ProdNetworkConnection.json");
        hyperLedgerFabricProperties.setCertificatePath("src/main/resources/crypto-config/prod-network/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/signcerts/User1@org1.example.com-cert.pem");
        hyperLedgerFabricProperties.setPrivateKeyPath("src/main/resources/crypto-config/prod-network/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/keystore/priv_sk");
        hyperLedgerFabricProperties.setTlsCertPath("src/main/resources/crypto-config/prod-network/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt");
        hyperLedgerFabricProperties.setChannel("businesschannel");

        BufferedReader certificateReader = Files.newBufferedReader(Paths.get(hyperLedgerFabricProperties.getCertificatePath()), StandardCharsets.UTF_8);

        X509Certificate certificate = Identities.readX509Certificate(certificateReader);

        BufferedReader privateKeyReader = Files.newBufferedReader(Paths.get(hyperLedgerFabricProperties.getPrivateKeyPath()), StandardCharsets.UTF_8);

        PrivateKey privateKey = Identities.readPrivateKey(privateKeyReader);

        Gateway gateway = Gateway.newInstance()
                .identity(new X509Identity(hyperLedgerFabricProperties.getMspId(), certificate))//向Gateway中导入账户的身份证书
                .signer(Signers.newPrivateKeySigner(privateKey))//设置Signer是向Gateway中导入账户与证书相匹配的私钥
                .connection(newGrpcConnection())//设置gRPC Channel
                .evaluateOptions(CallOption.deadlineAfter(5, TimeUnit.SECONDS))//设置gRPC任务超时时间,指定执行超时时间为5s
                .endorseOptions(CallOption.deadlineAfter(15, TimeUnit.SECONDS))//指定背书超时时间为15s
                .submitOptions(CallOption.deadlineAfter(5, TimeUnit.SECONDS))//指定提交交易超时时间为5s
                .commitStatusOptions(CallOption.deadlineAfter(1, TimeUnit.MINUTES))//
                .connect();

        log.info("=========================================== connected fabric gateway {} ", gateway);

        return gateway;
    }

    //创建 gRPC channel。ManagedChannel类，其实就是 gRPC Channel，只是在其基础上做了封装
    private ManagedChannel newGrpcConnection() throws IOException, CertificateException {
        Reader tlsCertReader = Files.newBufferedReader(Paths.get(hyperLedgerFabricProperties.getTlsCertPath()));
        X509Certificate tlsCert = Identities.readX509Certificate(tlsCertReader);

        return NettyChannelBuilder.forTarget("120.46.223.77:7051")
                .sslContext(GrpcSslContexts.forClient().trustManager(tlsCert).build())
                .overrideAuthority("peer0.org1.example.com")
                .build();
    }

    @Bean(name = "Org1ChaincodeEventListener")
    public ChaincodeEventListener chaincodeEventListener() {
        return new ChaincodeEventListener();
    }
}
