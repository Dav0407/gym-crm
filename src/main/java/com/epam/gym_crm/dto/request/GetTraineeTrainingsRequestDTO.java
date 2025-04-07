package com.epam.gym_crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetTraineeTrainingsRequestDTO {

    @NotBlank(message = "Trainee username is can not be null or empty")
    private String traineeUsername;

    private Date from;
    private Date to;
    private String trainerUsername;
    private String trainingType;
}
