package com.epam.gym_crm.service.impl;

import com.epam.gym_crm.dto.request.CreateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.response.TraineeProfileResponseDTO;
import com.epam.gym_crm.dto.response.TraineeResponseDTO;
import com.epam.gym_crm.entity.Trainee;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.exception.UserNotFoundException;
import com.epam.gym_crm.mapper.TraineeMapper;
import com.epam.gym_crm.repository.TraineeRepository;
import com.epam.gym_crm.service.JwtService;
import com.epam.gym_crm.service.TraineeService;
import com.epam.gym_crm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {

    private final JwtService jwtService;
    private final TraineeRepository traineeRepository;
    private final TraineeMapper traineeMapper;
    private final UserService userService;

    @Transactional
    @Override
    public TraineeResponseDTO createTraineeProfile(CreateTraineeProfileRequestDTO request) {
        log.info("Creating new trainee profile for: {} {}", request.getFirstName(), request.getLastName());

        User user = createUser(request.getFirstName(), request.getLastName());

        Trainee trainee = Trainee.builder()
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress().trim())
                .user(user)
                .build();

        Trainee savedTrainee = traineeRepository.save(trainee);

        log.info("Trainee profile created successfully: {}", savedTrainee);


        TraineeResponseDTO response = getTraineeResponseDTO(savedTrainee);

        return addTokensToDTO(savedTrainee, response);
    }

    @Override
    public TraineeResponseDTO getTraineeById(Long id) {

        log.info("Fetching trainee by ID: {}", id);

        Trainee trainee = traineeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trainee not found with ID: " + id));

        return getTraineeResponseDTO(trainee);
    }

    @Override
    public TraineeProfileResponseDTO getTraineeByUsername(String username) {

        User userByUsername = userService.getUserByUsername(username);

        Trainee trainee = traineeRepository.findByUser_Id(userByUsername.getId())
                .orElseThrow(() -> new UserNotFoundException("Trainee not found with username: " + userByUsername.getUsername()));

        return traineeMapper.toTraineeProfileResponseDTO(trainee);
    }

    @Override
    public Trainee getTraineeEntityByUsername(String username) {
        User userByUsername = userService.getUserByUsername(username);

        return traineeRepository.findByUser_Id(userByUsername.getId())
                .orElseThrow(() -> new UserNotFoundException("Trainee not found with username: " + userByUsername.getUsername()));
    }

    @Transactional
    @Override
    public TraineeProfileResponseDTO updateTraineeProfile(UpdateTraineeProfileRequestDTO request) {
        Trainee trainee = traineeRepository.findByUser_Username(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Trainee not found with username: " + request.getUsername()));

        trainee.getUser().setFirstName(request.getFirstName().trim());
        trainee.getUser().setLastName(request.getLastName().trim());
        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress().trim());

        return traineeMapper.toTraineeProfileResponseDTO(trainee);
    }

    @Override
    public void updateStatus(String username) {
        userService.updateStatus(username);
    }

    @Transactional
    @Override
    public TraineeProfileResponseDTO deleteTraineeProfileByUsername(String username) {
        TraineeProfileResponseDTO traineeByUsername = getTraineeByUsername(username);
        userService.deleteUser(username);
        return traineeByUsername;
    }

    @Override
    public UserService getUserService() {
        return userService;
    }

    private TraineeResponseDTO getTraineeResponseDTO(Trainee trainee) {
        return traineeMapper.toTraineeResponseDTO(trainee);
    }

    private TraineeResponseDTO addTokensToDTO(Trainee trainee, TraineeResponseDTO response) {

        String accessToken = jwtService.generateAccessToken(trainee.getUser());
        log.info("Access token generated successfully");

        String refreshToken = jwtService.generateRefreshToken(trainee.getUser());
        log.info("Refresh token generated successfully");

        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);

        return response;
    }
}
