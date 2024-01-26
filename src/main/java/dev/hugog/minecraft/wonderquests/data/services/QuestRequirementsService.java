package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestRequirementsRepository;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This class provides services for managing quest requirements in the game.
 */
public class QuestRequirementsService {

  private final QuestRequirementsRepository questRequirementsRepository;

  /**
   * Constructor for the QuestRequirementsService class.
   *
   * @param questRequirementsRepository The repository instance used for database operations related to quest requirements.
   */
  @Inject
  public QuestRequirementsService(QuestRequirementsRepository questRequirementsRepository) {
    this.questRequirementsRepository = questRequirementsRepository;
  }

  /**
   * This method deletes a quest requirement.
   *
   * @param requirementId The id of the quest requirement.
   * @return a CompletableFuture that will be completed when the quest requirement is deleted.
   */
  public CompletableFuture<Void> deleteRequirement(Integer requirementId) {
    return questRequirementsRepository.delete(requirementId);
  }

  /**
   * This method checks if a quest requirement exists.
   *
   * @param requirementId The id of the quest requirement.
   * @return a CompletableFuture that will be completed with a boolean indicating if the quest requirement exists.
   */
  public CompletableFuture<Boolean> checkIfRequirementExists(Integer requirementId) {
    return questRequirementsRepository.findById(requirementId)
        .thenApply(Optional::isPresent);
  }

}