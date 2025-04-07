package com.epam.gym_crm.controller;

import com.epam.gym_crm.dto.request.AddTrainingRequestDTO;
import com.epam.gym_crm.dto.request.GetTraineeTrainingsRequestDTO;
import com.epam.gym_crm.dto.request.GetTrainerTrainingsRequestDTO;
import com.epam.gym_crm.dto.response.TraineeTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainerTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainingTypeResponseDTO;
import com.epam.gym_crm.service.TrainingService;
import com.epam.gym_crm.service.TrainingTypeService;
import com.epam.gym_crm.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Training API")
@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final UserService userService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingResponseDTO> addTraining(@Valid @RequestBody AddTrainingRequestDTO request,
                                                           @RequestHeader(value = "Username") String headerUsername,
                                                           @RequestHeader(value = "Password") String headerPassword) {
        userService.validateCredentials(headerUsername, headerPassword);
        TrainingResponseDTO response = trainingService.addTraining(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = "/trainees", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TraineeTrainingResponseDTO>> getTraineeTrainings(@Valid @RequestBody GetTraineeTrainingsRequestDTO request,
                                                                                @RequestHeader(value = "Username") String headerUsername,
                                                                                @RequestHeader(value = "Password") String headerPassword) {
        userService.validateCredentials(headerUsername, headerPassword);
        List<TraineeTrainingResponseDTO> response = trainingService.getTraineeTrainings(request);
        return ResponseEntity.status(HttpStatus.FOUND).body(response);
    }

    @GetMapping(value = "/trainers", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TrainerTrainingResponseDTO>> getTrainerTrainings(@Valid @RequestBody GetTrainerTrainingsRequestDTO request,
                                                                                @RequestHeader(value = "Username") String headerUsername,
                                                                                @RequestHeader(value = "Password") String headerPassword) {
        userService.validateCredentials(headerUsername, headerPassword);
        List<TrainerTrainingResponseDTO> response = trainingService.getTrainerTrainings(request);
        return ResponseEntity.status(HttpStatus.FOUND).body(response);
    }

    @GetMapping(value = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TrainingTypeResponseDTO>> getTrainingTypes(@RequestHeader(value = "Username") String headerUsername,
                                                                          @RequestHeader(value = "Password") String headerPassword) {
        userService.validateCredentials(headerUsername, headerPassword);
        List<TrainingTypeResponseDTO> response = trainingTypeService.getAllTrainingTypes();
        return ResponseEntity.status(HttpStatus.FOUND).body(response);
    }
}
