package org.example.hyperledgerfabric.app.javademo;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

//@Configuration
//@ConfigurationProperties(prefix = "fabric")
//@Data
@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
public class HyperLedgerFabricProperties {

    String mspId;

    String networkConnectionConfigPath;

    String certificatePath;

    String privateKeyPath;

    String tlsCertPath;

    String channel;
}
