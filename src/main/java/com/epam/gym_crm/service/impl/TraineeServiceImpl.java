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
import com.epam.gym_crm.service.TraineeService;
import com.epam.gym_crm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {

    private final TraineeRepository traineeRepository;
    private final TraineeMapper traineeMapper;
    private final UserService userService;

    @Transactional
    @Override
    public TraineeResponseDTO createTraineeProfile(CreateTraineeProfileRequestDTO request) {
        log.info("Creating new trainee profile for: {} {}", request.getFirstName(), request.getLastName());

        validateRequest(request);

        User user = createUser(request.getFirstName(), request.getLastName());

        Trainee trainee = Trainee.builder()
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress().trim())
                .user(user)
                .build();

        Trainee savedTrainee = traineeRepository.save(trainee);

        log.info("Trainee profile created successfully: {}", savedTrainee);
        return getTraineeResponseDTO(savedTrainee);
    }

    @Override
    public TraineeResponseDTO getTraineeById(Long id) {

        log.info("Fetching trainee by ID: {}", id);

        Trainee trainee = traineeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trainee not found with ID: " + id));

        return getTraineeResponseDTO(trainee);
    }

    @Transactional
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

    private void validateRequest(CreateTraineeProfileRequestDTO request) {
        if (!StringUtils.hasText(request.getFirstName()) || !StringUtils.hasText(request.getLastName())) {
            throw new IllegalArgumentException("First name and last name cannot be empty");
        }
        if (!StringUtils.hasText(request.getAddress())) {
            throw new IllegalArgumentException("Address cannot be empty");
        }
        if (request.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Date of birth is required");
        }
    }

    @Override
    public TraineeResponseDTO getTraineeResponseDTO(Trainee trainee) {
        return traineeMapper.toTraineeResponseDTO(trainee);
    }

    @Override
    public UserService getUserService() {
        return userService;
    }

    @Transactional
    @Override
    public boolean healthCheck() {
        try {
            // 1. Check database connection by performing a simple count query
            long traineeCount = traineeRepository.count();
            log.info("Health check: Found {} trainees in database", traineeCount);

            // 2. Verify we can create and delete a test trainee
            String testUsername = "healthcheck-user-" + System.currentTimeMillis();
            CreateTraineeProfileRequestDTO testRequest = new CreateTraineeProfileRequestDTO(
                    "Health",
                    "Check",
                    new Date("2025-01-02"),
                    "Tashkent"
            );

            // 3. Test creation
            TraineeResponseDTO created = createTraineeProfile(testRequest);
            if (created == null || !testUsername.equals(created.getUsername())) {
                log.error("Health check failed: Trainee creation test failed");
                return false;
            }

            // 4. Test retrieval
            TraineeProfileResponseDTO retrieved = getTraineeByUsername(testUsername);
            if (retrieved == null || !testUsername.equals(retrieved.getUsername())) {
                log.error("Health check failed: Trainee retrieval test failed");
                return false;
            }

            // 5. Test deletion
            TraineeProfileResponseDTO deleted = deleteTraineeProfileByUsername(testUsername);
            if (deleted == null || !testUsername.equals(deleted.getUsername())) {
                log.error("Health check failed: Trainee deletion test failed");
                return false;
            }

            // 6. Verify the test trainee was actually deleted
            try {
                getTraineeByUsername(testUsername);
                log.error("Health check failed: Test trainee still exists after deletion");
                return false;
            } catch (UserNotFoundException e) {
                // This is expected - the trainee should not exist
            }

            log.info("Health check completed successfully");
            return true;

        } catch (Exception e) {
            log.error("Health check failed with exception: {}", e.getMessage(), e);
            return false;
        }
    }
}
