package dev.hugog.minecraft.wonderquests.data.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import dev.hugog.minecraft.wonderquests.data.models.QuestRequirementModel;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestRequirementsRepository;
import dev.hugog.minecraft.wonderquests.data.types.RequirementType;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QuestRequirementsServiceTest {

  @Mock
  private QuestRequirementsRepository questRequirementsRepository;

  @InjectMocks
  private QuestRequirementsService questRequirementsService;

  private QuestRequirementModel questRequirementModel;
  private Integer requirementId;

  @BeforeEach
  public void setUp() {
    requirementId = 1;
    questRequirementModel = new QuestRequirementModel(requirementId, 1,
        RequirementType.MONEY.name(), "", 0f);
  }

  @Test
  @DisplayName("Delete requirement is successful if is also successful in database layer")
  public void deleteRequirementSuccessfully() {

    when(questRequirementsRepository.delete(requirementId))
        .thenReturn(CompletableFuture.completedFuture(null));

    CompletableFuture<Void> result = questRequirementsService.deleteRequirement(requirementId);

    assertNull(result.join());

  }

  @Test
  @DisplayName("Check if requirement exists returns true")
  public void checkIfRequirementExistsReturnsTrue() {

    when(questRequirementsRepository.findById(requirementId))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(questRequirementModel)));

    CompletableFuture<Boolean> result = questRequirementsService
        .checkIfRequirementExists(requirementId);

    assertTrue(result.join());

  }

  @Test
  @DisplayName("Check if requirement exists returns false")
  public void checkIfRequirementExistsReturnsFalse() {

    when(questRequirementsRepository.findById(requirementId))
        .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

    CompletableFuture<Boolean> result = questRequirementsService
        .checkIfRequirementExists(requirementId);

    assertFalse(result.join());

  }
}