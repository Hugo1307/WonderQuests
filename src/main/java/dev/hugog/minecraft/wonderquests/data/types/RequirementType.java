package dev.hugog.minecraft.wonderquests.data.types;

public enum RequirementType {

  MONEY, ITEM, PERMISSION, EXPERIENCE;

  public static RequirementType fromString(String string) {
    for (RequirementType requirementType : RequirementType.values()) {
      if (requirementType.name().equalsIgnoreCase(string)) {
        return requirementType;
      }
    }
    return null;
  }

}
