package org.example.hyperledgerfabric.app.javademo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CourseDTO {

    String courseKey;

    String courseName;

    String courseObjectives;

    String employerEvaluation;
}
