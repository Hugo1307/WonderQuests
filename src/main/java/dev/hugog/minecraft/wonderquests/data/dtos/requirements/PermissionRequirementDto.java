package dev.hugog.minecraft.wonderquests.data.dtos.requirements;

import dev.hugog.minecraft.wonderquests.data.types.RequirementType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PermissionRequirementDto extends QuestRequirementDto {

  private final Integer id;
  private final Integer questId;
  private final RequirementType type;
  private final String permission;

  public PermissionRequirementDto(Integer id, Integer questId, String permission) {
    super(id, questId, RequirementType.PERMISSION, permission, null);
    this.id = id;
    this.questId = questId;
    this.type = RequirementType.PERMISSION;
    this.permission = permission;
  }

}
