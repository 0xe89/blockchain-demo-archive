package org.example;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
@Data
@Accessors(chain = true)
public class Course {
    @Property
    String courseKey;

    @Property
    String courseName;

    @Property
    String courseObjectives;

    @Property
    String employerEvaluation;

}
