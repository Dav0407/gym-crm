package com.epam.gym_crm.controller;

import com.epam.gym_crm.dto.request.CreateTrainerProfileRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTrainerListRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTrainerProfileRequestDTO;
import com.epam.gym_crm.dto.response.TrainerProfileResponseDTO;
import com.epam.gym_crm.dto.response.TrainerResponseDTO;
import com.epam.gym_crm.dto.response.TrainerSecureResponseDTO;
import com.epam.gym_crm.service.TraineeTrainerService;
import com.epam.gym_crm.service.TrainerService;
import com.epam.gym_crm.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Trainer API")
@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final UserService userService;
    private final TrainerService trainerService;
    private final TraineeTrainerService traineeTrainerService;

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainerResponseDTO> registerTrainer(@Valid @RequestBody CreateTrainerProfileRequestDTO request) {
        TrainerResponseDTO response = trainerService.createTrainerProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainerProfileResponseDTO> getTrainerProfile(@PathVariable("username") @NotBlank(message = "Username is required") String username,
                                                                       @RequestHeader(value = "Username") String headerUsername,
                                                                       @RequestHeader(value = "Password") String headerPassword) {
        userService.validateCredentials(headerUsername, headerPassword);
        TrainerProfileResponseDTO response = trainerService.getTrainerByUsername(username);
        return ResponseEntity.status(HttpStatus.FOUND).body(response);
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainerProfileResponseDTO> updateTrainerProfile(@Valid @RequestBody UpdateTrainerProfileRequestDTO request,
                                                                          @RequestHeader(value = "Username") String headerUsername,
                                                                          @RequestHeader(value = "Password") String headerPassword) {
        userService.validateCredentials(headerUsername, headerPassword);
        TrainerProfileResponseDTO response = trainerService.updateTrainerProfile(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping(value = "/not-assigned/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TrainerSecureResponseDTO>> getNotAssignedTrainers(@PathVariable("username") @NotBlank(message = "Username is required") String username,
                                                                                 @RequestHeader(value = "Username") String headerUsername,
                                                                                 @RequestHeader(value = "Password") String headerPassword) {
        userService.validateCredentials(headerUsername, headerPassword);
        List<TrainerSecureResponseDTO> response = trainerService.getNotAssignedTrainersByTraineeUsername(username);
        return ResponseEntity.status(HttpStatus.FOUND).body(response);
    }

    @PutMapping(value = "/assign", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TrainerSecureResponseDTO>> updateTraineesTrainersList(@Valid @RequestBody UpdateTrainerListRequestDTO request,
                                                                                     @RequestHeader(value = "Username") String headerUsername,
                                                                                     @RequestHeader(value = "Password") String headerPassword) {
        userService.validateCredentials(headerUsername, headerPassword);
        List<TrainerSecureResponseDTO> response = traineeTrainerService.updateTraineeTrainers(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PatchMapping(value = "/{trainer-username}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainerProfileResponseDTO> switchTraineeStatus(@PathVariable("trainer-username") @NotBlank(message = "Username is required") String trainerUsername,
                                                                         @RequestHeader(value = "Username") String headerUsername,
                                                                         @RequestHeader(value = "Password") String headerPassword) {
        userService.validateCredentials(headerUsername, headerPassword);
        trainerService.updateStatus(trainerUsername);
        TrainerProfileResponseDTO response = trainerService.getTrainerByUsername(trainerUsername);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
