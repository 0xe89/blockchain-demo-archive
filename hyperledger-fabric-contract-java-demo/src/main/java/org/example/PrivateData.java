package org.example;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
@Data
@Accessors(chain = true)
public class PrivateData {

    @Property
    String username;
    @Property
    String password;
    @Property
    String collection;
}
