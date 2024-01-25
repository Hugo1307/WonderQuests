package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestRequirementsRepository;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class QuestRequirementsService {

  private final QuestRequirementsRepository questRequirementsRepository;

  @Inject
  public QuestRequirementsService(QuestRequirementsRepository questRequirementsRepository) {
    this.questRequirementsRepository = questRequirementsRepository;
  }

  public CompletableFuture<Void> deleteRequirement(Integer requirementId) {
    return questRequirementsRepository.delete(requirementId);
  }

  public CompletableFuture<Boolean> checkIfRequirementExists(Integer requirementId) {
    return questRequirementsRepository.findById(requirementId)
        .thenApply(Optional::isPresent);
  }

}
