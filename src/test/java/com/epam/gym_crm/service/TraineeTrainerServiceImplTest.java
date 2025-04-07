package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.UpdateTrainerListRequestDTO;
import com.epam.gym_crm.dto.response.TrainerSecureResponseDTO;
import com.epam.gym_crm.entity.Trainee;
import com.epam.gym_crm.entity.TraineeTrainer;
import com.epam.gym_crm.entity.Trainer;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.mapper.TrainerMapper;
import com.epam.gym_crm.repository.TraineeTrainerRepository;
import com.epam.gym_crm.service.impl.TraineeTrainerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeTrainerServiceImplTest {

    @Mock
    private TraineeTrainerRepository traineeTrainerRepository;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainerMapper trainerMapper;

    @InjectMocks
    private TraineeTrainerServiceImpl traineeTrainerService;

    private Trainee trainee;
    private Trainer trainer;
    private TraineeTrainer traineeTrainer;

    @BeforeEach
    void setUp() {
        trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUser(new User());
        trainee.getUser().setUsername("trainee.username");

        trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUser(new User());
        trainer.getUser().setUsername("trainer.username");

        traineeTrainer = new TraineeTrainer();
        traineeTrainer.setId(1L);
        traineeTrainer.setTrainee(trainee);
        traineeTrainer.setTrainer(trainer);
    }

    @Test
    void testCreateTraineeTrainer_Success() {
        when(traineeService.getTraineeEntityByUsername("trainee.username")).thenReturn(trainee);
        when(trainerService.getTrainerEntityByUsername("trainer.username")).thenReturn(trainer);
        when(traineeTrainerRepository.findByTraineeAndTrainer(trainee, trainer)).thenReturn(Optional.empty());
        when(traineeTrainerRepository.save(any(TraineeTrainer.class))).thenReturn(traineeTrainer);

        TraineeTrainer result = traineeTrainerService.createTraineeTrainer("trainee.username", "trainer.username");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(trainee, result.getTrainee());
        assertEquals(trainer, result.getTrainer());

        verify(traineeService, times(1)).getTraineeEntityByUsername("trainee.username");
        verify(trainerService, times(1)).getTrainerEntityByUsername("trainer.username");
        verify(traineeTrainerRepository, times(1)).findByTraineeAndTrainer(trainee, trainer);
        verify(traineeTrainerRepository, times(1)).save(any(TraineeTrainer.class));
    }

    @Test
    void testCreateTraineeTrainer_RelationshipAlreadyExists() {
        when(traineeService.getTraineeEntityByUsername("trainee.username")).thenReturn(trainee);
        when(trainerService.getTrainerEntityByUsername("trainer.username")).thenReturn(trainer);
        when(traineeTrainerRepository.findByTraineeAndTrainer(trainee, trainer)).thenReturn(Optional.of(traineeTrainer));

        TraineeTrainer result = traineeTrainerService.createTraineeTrainer("trainee.username", "trainer.username");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(trainee, result.getTrainee());
        assertEquals(trainer, result.getTrainer());

        verify(traineeService, times(1)).getTraineeEntityByUsername("trainee.username");
        verify(trainerService, times(1)).getTrainerEntityByUsername("trainer.username");
        verify(traineeTrainerRepository, times(1)).findByTraineeAndTrainer(trainee, trainer);
        verify(traineeTrainerRepository, times(0)).save(any(TraineeTrainer.class));
    }

    @Test
    void testCreateTraineeTrainer_InvalidTraineeUsername() {
        assertThrows(IllegalArgumentException.class, () -> traineeTrainerService.createTraineeTrainer(null, "trainer.username"));
        assertThrows(IllegalArgumentException.class, () -> traineeTrainerService.createTraineeTrainer("", "trainer.username"));

        verify(traineeService, times(0)).getTraineeEntityByUsername(anyString());
        verify(trainerService, times(0)).getTrainerEntityByUsername(anyString());
        verify(traineeTrainerRepository, times(0)).findByTraineeAndTrainer(any(), any());
        verify(traineeTrainerRepository, times(0)).save(any(TraineeTrainer.class));
    }

    @Test
    void testCreateTraineeTrainer_InvalidTrainerUsername() {
        assertThrows(IllegalArgumentException.class, () -> traineeTrainerService.createTraineeTrainer("trainee.username", null));
        assertThrows(IllegalArgumentException.class, () -> traineeTrainerService.createTraineeTrainer("trainee.username", ""));

        verify(traineeService, times(0)).getTraineeEntityByUsername(anyString());
        verify(trainerService, times(0)).getTrainerEntityByUsername(anyString());
        verify(traineeTrainerRepository, times(0)).findByTraineeAndTrainer(any(), any());
        verify(traineeTrainerRepository, times(0)).save(any(TraineeTrainer.class));
    }

    @Test
    void testFindByTraineeUsername_Success() {
        when(traineeTrainerRepository.findAllByTrainee_User_Username("trainee.username")).thenReturn(Collections.singletonList(traineeTrainer));

        List<TraineeTrainer> result = traineeTrainerService.findByTraineeUsername("trainee.username");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(traineeTrainer, result.get(0));

        verify(traineeTrainerRepository, times(1)).findAllByTrainee_User_Username("trainee.username");
    }

    @Test
    void testUpdateTraineeTrainers_Success() {

        List<TraineeTrainer> existingRelations = Collections.singletonList(traineeTrainer);

        TrainerSecureResponseDTO trainerResponseDTO = new TrainerSecureResponseDTO();

        when(traineeService.getTraineeEntityByUsername("trainee.username")).thenReturn(trainee);
        when(trainerService.getTrainerEntityByUsername("trainer.username")).thenReturn(trainer);
        when(traineeTrainerRepository.findAllByTrainee_User_Username("trainee.username")).thenReturn(existingRelations);
        when(trainerMapper.toTrainerSecureResponseDTO(trainer)).thenReturn(trainerResponseDTO);

        List<TrainerSecureResponseDTO> result = traineeTrainerService.updateTraineeTrainers(new UpdateTrainerListRequestDTO("trainee.username",
                Collections.singletonList("trainer.username")));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(trainerResponseDTO, result.get(0));

        verify(traineeService, times(1)).getTraineeEntityByUsername("trainee.username");
        verify(trainerService, times(1)).getTrainerEntityByUsername("trainer.username");
        verify(traineeTrainerRepository, times(1)).findAllByTrainee_User_Username("trainee.username");
        verify(traineeTrainerRepository, times(1)).deleteAll(existingRelations);
        verify(traineeTrainerRepository, times(1)).saveAll(any());
        verify(trainerMapper, times(1)).toTrainerSecureResponseDTO(trainer);
    }

    @Test
    void testUpdateTraineeTrainers_InvalidTraineeUsername() {
        assertThrows(IllegalArgumentException.class, () -> traineeTrainerService.updateTraineeTrainers(new UpdateTrainerListRequestDTO(null, Collections.singletonList("trainer.username"))));
        assertThrows(IllegalArgumentException.class, () -> traineeTrainerService.updateTraineeTrainers(new UpdateTrainerListRequestDTO("", Collections.singletonList("trainer.username"))));

        verify(traineeService, times(0)).getTraineeEntityByUsername(anyString());
        verify(trainerService, times(0)).getTrainerEntityByUsername(anyString());
        verify(traineeTrainerRepository, times(0)).findAllByTrainee_User_Username(anyString());
        verify(traineeTrainerRepository, times(0)).deleteAll(any());
        verify(traineeTrainerRepository, times(0)).saveAll(any());
    }

    @Test
    void testUpdateTraineeTrainers_InvalidTrainerUsernames() {
        assertThrows(IllegalArgumentException.class, () -> traineeTrainerService.updateTraineeTrainers(new UpdateTrainerListRequestDTO("trainee.username", null)));

        verify(traineeService, times(0)).getTraineeEntityByUsername(anyString());
        verify(trainerService, times(0)).getTrainerEntityByUsername(anyString());
        verify(traineeTrainerRepository, times(0)).findAllByTrainee_User_Username(anyString());
        verify(traineeTrainerRepository, times(0)).deleteAll(any());
        verify(traineeTrainerRepository, times(0)).saveAll(any());
    }
}