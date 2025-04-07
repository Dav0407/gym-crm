package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.AddTrainingRequestDTO;
import com.epam.gym_crm.dto.request.GetTraineeTrainingsRequestDTO;
import com.epam.gym_crm.dto.request.GetTrainerTrainingsRequestDTO;
import com.epam.gym_crm.dto.response.TraineeProfileResponseDTO;
import com.epam.gym_crm.dto.response.TraineeResponseDTO;
import com.epam.gym_crm.dto.response.TraineeTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainerProfileResponseDTO;
import com.epam.gym_crm.dto.response.TrainerResponseDTO;
import com.epam.gym_crm.dto.response.TrainerTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainingResponseDTO;
import com.epam.gym_crm.entity.Trainee;
import com.epam.gym_crm.entity.Trainer;
import com.epam.gym_crm.entity.Training;
import com.epam.gym_crm.entity.TrainingType;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.mapper.TrainingMapper;
import com.epam.gym_crm.repository.TrainingRepository;
import com.epam.gym_crm.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    private TraineeTrainerService traineeTrainerService;

    @Mock
    private TrainingMapper trainingMapper;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Trainee traineeEntity;
    private Trainer trainerEntity;
    private TraineeProfileResponseDTO traineeProfileResponseDTO;
    private TrainerProfileResponseDTO trainerProfileResponseDTO;
    private Training training;
    private TrainingType trainingType;
    private Date trainingDate;

    @BeforeEach
    void setUp() {
        trainingDate = new Date();

        // Create User entities
        User traineeUser = new User();
        traineeUser.setUsername("trainee.username");

        User trainerUser = new User();
        trainerUser.setUsername("trainer.username");

        // Create entities
        traineeEntity = new Trainee();
        traineeEntity.setId(1L);
        traineeEntity.setUser(traineeUser);

        trainerEntity = new Trainer();
        trainerEntity.setId(1L);
        trainerEntity.setUser(trainerUser);
        trainerEntity.setSpecialization(TrainingType.builder().trainingTypeName("Cardio").build());

        trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Cardio");

        training = new Training();
        training.setId(1L);
        training.setTrainee(traineeEntity);
        training.setTrainer(trainerEntity);
        training.setTrainingType(trainingType);
        training.setTrainingDate(trainingDate);
        training.setTrainingDuration(60);
        training.setTrainingName("Morning Run");

        // Create response DTOs
        TraineeResponseDTO traineeResponseDTO = new TraineeResponseDTO();
        traineeResponseDTO.setId(traineeEntity.getId());
        traineeResponseDTO.setUsername(traineeEntity.getUser().getUsername());

        traineeProfileResponseDTO = new TraineeProfileResponseDTO();
        traineeProfileResponseDTO.setId(traineeEntity.getId());
        traineeProfileResponseDTO.setUsername(traineeEntity.getUser().getUsername());

        TrainerResponseDTO trainerResponseDTO = new TrainerResponseDTO();
        trainerResponseDTO.setId(trainerEntity.getId());
        trainerResponseDTO.setUsername(trainerEntity.getUser().getUsername());

        trainerProfileResponseDTO = new TrainerProfileResponseDTO();
        trainerProfileResponseDTO.setId(trainerEntity.getId());
        trainerProfileResponseDTO.setUsername(trainerEntity.getUser().getUsername());
    }

    @Test
    void testGetTraineeTrainings_Success() {
        // Arrange
        GetTraineeTrainingsRequestDTO request = new GetTraineeTrainingsRequestDTO();
        request.setTraineeUsername("trainee.username");
        request.setFrom(new Date(System.currentTimeMillis() - 1000)); // Past date
        request.setTo(new Date());

        TraineeTrainingResponseDTO mockTrainingResponseDTO = new TraineeTrainingResponseDTO();
        mockTrainingResponseDTO.setTrainingName("Morning Run");
        mockTrainingResponseDTO.setTrainingType("Cardio");

        when(trainingRepository.findAllTraineeTrainings(
                "trainee.username", null, request.getFrom(), request.getTo(), null))
                .thenReturn(Collections.singletonList(training));

        when(trainingMapper.toTraineeTrainingResponseDTO(training))
                .thenReturn(mockTrainingResponseDTO);

        // Act
        List<TraineeTrainingResponseDTO> result = trainingService.getTraineeTrainings(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Morning Run", result.get(0).getTrainingName());
        assertEquals("Cardio", result.get(0).getTrainingType());

        verify(trainingRepository, times(1)).findAllTraineeTrainings(
                "trainee.username", null, request.getFrom(), request.getTo(), null);
        verify(trainingMapper, times(1)).toTraineeTrainingResponseDTO(training);
    }

    @Test
    void testGetTraineeTrainings_InvalidTraineeUsername() {
        // Arrange
        GetTraineeTrainingsRequestDTO request = new GetTraineeTrainingsRequestDTO();
        request.setTraineeUsername("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> trainingService.getTraineeTrainings(request));

        verify(trainingRepository, times(0)).findAllTraineeTrainings(
                anyString(), anyString(), any(), any(), anyString());
    }

    @Test
    void testGetTraineeTrainings_InvalidDateRange() {
        // Arrange
        GetTraineeTrainingsRequestDTO request = new GetTraineeTrainingsRequestDTO();
        request.setTraineeUsername("trainee.username");
        request.setFrom(new Date(System.currentTimeMillis() + 1000)); // Future date
        request.setTo(new Date());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> trainingService.getTraineeTrainings(request));

        verify(trainingRepository, times(0)).findAllTraineeTrainings(
                anyString(), anyString(), any(), any(), anyString());
    }

    @Test
    void testGetTraineeTrainings_WithTrainerUsername() {
        // Arrange
        GetTraineeTrainingsRequestDTO request = new GetTraineeTrainingsRequestDTO();
        request.setTraineeUsername("trainee.username");
        request.setTrainerUsername("trainer.username");

        when(trainerService.getTrainerByUsername("trainer.username")).thenReturn(trainerProfileResponseDTO);
        when(trainingRepository.findAllTraineeTrainings(
                "trainee.username", "trainer.username", null, null, null))
                .thenReturn(Collections.singletonList(training));

        TraineeTrainingResponseDTO mockTrainingResponseDTO = new TraineeTrainingResponseDTO();
        mockTrainingResponseDTO.setTrainingName("Morning Run");
        when(trainingMapper.toTraineeTrainingResponseDTO(training))
                .thenReturn(mockTrainingResponseDTO);

        // Act
        List<TraineeTrainingResponseDTO> result = trainingService.getTraineeTrainings(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(trainerService, times(1)).getTrainerByUsername("trainer.username");
    }

    @Test
    void testGetTraineeTrainings_TrainerNotFound() {
        // Arrange
        GetTraineeTrainingsRequestDTO request = new GetTraineeTrainingsRequestDTO();
        request.setTraineeUsername("trainee.username");
        request.setTrainerUsername("non.existent.trainer");

        when(trainerService.getTrainerByUsername("non.existent.trainer")).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> trainingService.getTraineeTrainings(request));

        verify(trainerService, times(1)).getTrainerByUsername("non.existent.trainer");
        verify(trainingRepository, times(0)).findAllTraineeTrainings(
                anyString(), anyString(), any(), any(), anyString());
    }

    @Test
    void testGetTrainerTrainings_Success() {
        // Arrange
        GetTrainerTrainingsRequestDTO request = new GetTrainerTrainingsRequestDTO();
        request.setTrainerUsername("trainer.username");
        request.setFrom(new Date(System.currentTimeMillis() - 1000)); // Past date
        request.setTo(new Date());

        TrainerTrainingResponseDTO mockTrainingResponseDTO = new TrainerTrainingResponseDTO();
        mockTrainingResponseDTO.setTrainingName("Morning Run");
        mockTrainingResponseDTO.setTrainingType("Cardio");

        when(trainerService.getTrainerByUsername("trainer.username")).thenReturn(trainerProfileResponseDTO);
        when(trainingRepository.findAllTrainerTrainings(
                "trainer.username", null, request.getFrom(), request.getTo()))
                .thenReturn(Collections.singletonList(training));

        when(trainingMapper.toTrainerTrainingResponseDTO(training))
                .thenReturn(mockTrainingResponseDTO);

        // Act
        List<TrainerTrainingResponseDTO> result = trainingService.getTrainerTrainings(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Morning Run", result.get(0).getTrainingName());

        verify(trainerService, times(1)).getTrainerByUsername("trainer.username");
        verify(trainingRepository, times(1)).findAllTrainerTrainings(
                "trainer.username", null, request.getFrom(), request.getTo());
        verify(trainingMapper, times(1)).toTrainerTrainingResponseDTO(training);
    }

    @Test
    void testGetTrainerTrainings_InvalidTrainerUsername() {
        // Arrange
        GetTrainerTrainingsRequestDTO request = new GetTrainerTrainingsRequestDTO();
        request.setTrainerUsername("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> trainingService.getTrainerTrainings(request));

        verify(trainingRepository, times(0)).findAllTrainerTrainings(
                anyString(), anyString(), any(), any());
    }

    @Test
    void testGetTrainerTrainings_InvalidDateRange() {
        // Arrange
        GetTrainerTrainingsRequestDTO request = new GetTrainerTrainingsRequestDTO();
        request.setTrainerUsername("trainer.username");
        request.setFrom(new Date(System.currentTimeMillis() + 1000)); // Future date
        request.setTo(new Date());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> trainingService.getTrainerTrainings(request));

        verify(trainingRepository, times(0)).findAllTrainerTrainings(
                anyString(), anyString(), any(), any());
    }

    @Test
    void testGetTrainerTrainings_TrainerNotFound() {
        // Arrange
        GetTrainerTrainingsRequestDTO request = new GetTrainerTrainingsRequestDTO();
        request.setTrainerUsername("non.existent.trainer");

        when(trainerService.getTrainerByUsername("non.existent.trainer")).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> trainingService.getTrainerTrainings(request));

        verify(trainerService, times(1)).getTrainerByUsername("non.existent.trainer");
        verify(trainingRepository, times(0)).findAllTrainerTrainings(
                anyString(), anyString(), any(), any());
    }

    @Test
    void testGetTrainerTrainings_WithTraineeUsername() {
        // Arrange
        GetTrainerTrainingsRequestDTO request = new GetTrainerTrainingsRequestDTO();
        request.setTrainerUsername("trainer.username");
        request.setTraineeUsername("trainee.username");

        when(trainerService.getTrainerByUsername("trainer.username")).thenReturn(trainerProfileResponseDTO);
        when(traineeService.getTraineeByUsername("trainee.username")).thenReturn(traineeProfileResponseDTO);

        when(trainingRepository.findAllTrainerTrainings(
                "trainer.username", "trainee.username", null, null))
                .thenReturn(Collections.singletonList(training));

        TrainerTrainingResponseDTO mockTrainingResponseDTO = new TrainerTrainingResponseDTO();
        mockTrainingResponseDTO.setTrainingName("Morning Run");
        when(trainingMapper.toTrainerTrainingResponseDTO(training))
                .thenReturn(mockTrainingResponseDTO);

        // Act
        List<TrainerTrainingResponseDTO> result = trainingService.getTrainerTrainings(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(traineeService, times(1)).getTraineeByUsername("trainee.username");
    }

    @Test
    void testGetTrainerTrainings_TraineeNotFound() {
        // Arrange
        GetTrainerTrainingsRequestDTO request = new GetTrainerTrainingsRequestDTO();
        request.setTrainerUsername("trainer.username");
        request.setTraineeUsername("non.existent.trainee");

        when(trainerService.getTrainerByUsername("trainer.username")).thenReturn(trainerProfileResponseDTO);
        when(traineeService.getTraineeByUsername("non.existent.trainee")).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> trainingService.getTrainerTrainings(request));

        verify(trainerService, times(1)).getTrainerByUsername("trainer.username");
        verify(traineeService, times(1)).getTraineeByUsername("non.existent.trainee");
        verify(trainingRepository, times(0)).findAllTrainerTrainings(
                anyString(), anyString(), any(), any());
    }

    @Test
    void testAddTraining_Success() {
        // Arrange
        AddTrainingRequestDTO request = new AddTrainingRequestDTO();
        request.setTraineeUsername("trainee.username");
        request.setTrainerUsername("trainer.username");
        request.setTrainingDate(trainingDate);
        request.setTrainingDuration(60);
        request.setTrainingName("Morning Run");

        // Create the training object that matches what will be passed to the mapper
        Training trainingToMap = new Training();
        trainingToMap.setTrainee(traineeEntity);
        trainingToMap.setTrainer(trainerEntity);
        trainingToMap.setTrainingType(trainingType);
        trainingToMap.setTrainingDate(trainingDate);
        trainingToMap.setTrainingDuration(60);
        trainingToMap.setTrainingName("Morning Run");

        TrainingResponseDTO mockTrainingResponseDTO = new TrainingResponseDTO();
        mockTrainingResponseDTO.setId(1L);
        mockTrainingResponseDTO.setTrainingName("Morning Run");
        mockTrainingResponseDTO.setTrainingType("Cardio");

        when(traineeService.getTraineeEntityByUsername("trainee.username")).thenReturn(traineeEntity);
        when(trainerService.getTrainerEntityByUsername("trainer.username")).thenReturn(trainerEntity);
        when(trainingTypeService.findByValue("Cardio")).thenReturn(Optional.of(trainingType));

        // Mock the save operation to return the training with ID
        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> {
            Training t = invocation.getArgument(0);
            Training saved = new Training();
            saved.setId(1L); // Set the ID
            saved.setTrainee(t.getTrainee());
            saved.setTrainer(t.getTrainer());
            saved.setTrainingType(t.getTrainingType());
            saved.setTrainingDate(t.getTrainingDate());
            saved.setTrainingDuration(t.getTrainingDuration());
            saved.setTrainingName(t.getTrainingName());
            return saved;
        });

        // Mock the mapper to expect the unsaved training (implementation passes the original training)
        when(trainingMapper.toTrainingResponseDTO(argThat(t -> t.getId() == null)))
                .thenReturn(mockTrainingResponseDTO);

        // Act
        TrainingResponseDTO result = trainingService.addTraining(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Morning Run", result.getTrainingName());
        assertEquals("Cardio", result.getTrainingType());

        // Verify interactions
        verify(traineeService).getTraineeEntityByUsername("trainee.username");
        verify(trainerService).getTrainerEntityByUsername("trainer.username");
        verify(trainingTypeService).findByValue("Cardio");
        verify(trainingRepository).save(any(Training.class));
        verify(traineeTrainerService).createTraineeTrainer("trainee.username", "trainer.username");
        verify(trainingMapper).toTrainingResponseDTO(argThat(t -> t.getId() == null));
    }
    @Test
    void testAddTraining_InvalidTraineeUsername() {
        // Arrange
        AddTrainingRequestDTO request = new AddTrainingRequestDTO();
        request.setTraineeUsername("");
        request.setTrainerUsername("trainer.username");
        request.setTrainingDate(trainingDate);
        request.setTrainingDuration(60);
        request.setTrainingName("Morning Run");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> trainingService.addTraining(request));

        verify(traineeService, times(0)).getTraineeEntityByUsername(anyString());
        verify(trainerService, times(0)).getTrainerEntityByUsername(anyString());
        verify(trainingTypeService, times(0)).findByValue(anyString());
        verify(trainingRepository, times(0)).save(any(Training.class));
        verify(traineeTrainerService, times(0)).createTraineeTrainer(anyString(), anyString());
    }

    @Test
    void testAddTraining_InvalidTrainerUsername() {
        // Arrange
        AddTrainingRequestDTO request = new AddTrainingRequestDTO();
        request.setTraineeUsername("trainee.username");
        request.setTrainerUsername("");
        request.setTrainingDate(trainingDate);
        request.setTrainingDuration(60);
        request.setTrainingName("Morning Run");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> trainingService.addTraining(request));

        verify(traineeService, times(0)).getTraineeEntityByUsername(anyString());
        verify(trainerService, times(0)).getTrainerEntityByUsername(anyString());
        verify(trainingTypeService, times(0)).findByValue(anyString());
        verify(trainingRepository, times(0)).save(any(Training.class));
        verify(traineeTrainerService, times(0)).createTraineeTrainer(anyString(), anyString());
    }

    @Test
    void testAddTraining_TrainingDateNull() {
        // Arrange
        AddTrainingRequestDTO request = new AddTrainingRequestDTO();
        request.setTraineeUsername("trainee.username");
        request.setTrainerUsername("trainer.username");
        request.setTrainingDate(null);
        request.setTrainingDuration(60);
        request.setTrainingName("Morning Run");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> trainingService.addTraining(request));

        verify(traineeService, times(0)).getTraineeEntityByUsername(anyString());
        verify(trainerService, times(0)).getTrainerEntityByUsername(anyString());
    }

    @Test
    void testAddTraining_TrainingDurationNull() {
        // Arrange
        AddTrainingRequestDTO request = new AddTrainingRequestDTO();
        request.setTraineeUsername("trainee.username");
        request.setTrainerUsername("trainer.username");
        request.setTrainingDate(trainingDate);
        request.setTrainingDuration(null);
        request.setTrainingName("Morning Run");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> trainingService.addTraining(request));

        verify(traineeService, times(0)).getTraineeEntityByUsername(anyString());
        verify(trainerService, times(0)).getTrainerEntityByUsername(anyString());
    }

    @Test
    void testAddTraining_TraineeNotFound() {
        // Arrange
        AddTrainingRequestDTO request = new AddTrainingRequestDTO();
        request.setTraineeUsername("non.existent.trainee");
        request.setTrainerUsername("trainer.username");
        request.setTrainingDate(trainingDate);
        request.setTrainingDuration(60);
        request.setTrainingName("Morning Run");

        when(traineeService.getTraineeEntityByUsername("non.existent.trainee")).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> trainingService.addTraining(request));

        verify(traineeService, times(1)).getTraineeEntityByUsername("non.existent.trainee");
        verify(trainerService, times(0)).getTrainerEntityByUsername(anyString());
        verify(trainingTypeService, times(0)).findByValue(anyString());
    }

    @Test
    void testAddTraining_TrainerNotFound() {
        // Arrange
        AddTrainingRequestDTO request = new AddTrainingRequestDTO();
        request.setTraineeUsername("trainee.username");
        request.setTrainerUsername("non.existent.trainer");
        request.setTrainingDate(trainingDate);
        request.setTrainingDuration(60);
        request.setTrainingName("Morning Run");

        when(traineeService.getTraineeEntityByUsername("trainee.username")).thenReturn(traineeEntity);
        when(trainerService.getTrainerEntityByUsername("non.existent.trainer")).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> trainingService.addTraining(request));

        verify(traineeService, times(1)).getTraineeEntityByUsername("trainee.username");
        verify(trainerService, times(1)).getTrainerEntityByUsername("non.existent.trainer");
        verify(trainingTypeService, times(0)).findByValue(anyString());
    }

    @Test
    void testAddTraining_InvalidTrainingType() {
        // Arrange
        AddTrainingRequestDTO request = new AddTrainingRequestDTO();
        request.setTraineeUsername("trainee.username");
        request.setTrainerUsername("trainer.username");
        request.setTrainingDate(trainingDate);
        request.setTrainingDuration(60);
        request.setTrainingName("Morning Run");

        // Create a trainer with an invalid specialization
        Trainer trainerWithInvalidType = new Trainer();
        trainerWithInvalidType.setId(1L);
        trainerWithInvalidType.setUser(new User());
        trainerWithInvalidType.getUser().setUsername("trainer.username");
        trainerWithInvalidType.setSpecialization(TrainingType.builder().trainingTypeName("InvalidType").build());

        when(traineeService.getTraineeEntityByUsername("trainee.username")).thenReturn(traineeEntity);
        when(trainerService.getTrainerEntityByUsername("trainer.username")).thenReturn(trainerWithInvalidType);
        when(trainingTypeService.findByValue("InvalidType")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> trainingService.addTraining(request));

        verify(traineeService, times(1)).getTraineeEntityByUsername("trainee.username");
        verify(trainerService, times(1)).getTrainerEntityByUsername("trainer.username");
        verify(trainingTypeService, times(1)).findByValue("InvalidType");
        verify(trainingRepository, times(0)).save(any(Training.class));
        verify(traineeTrainerService, times(0)).createTraineeTrainer(anyString(), anyString());
    }
}