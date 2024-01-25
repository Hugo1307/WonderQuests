package dev.hugog.minecraft.wonderquests.data.types;

public enum ObjectiveType {

  BREAK_BLOCK, PLACE_BLOCK, KILL_MOBS;

  public static ObjectiveType fromString(String string) {
    for (ObjectiveType objectiveType : ObjectiveType.values()) {
      if (objectiveType.name().equalsIgnoreCase(string)) {
        return objectiveType;
      }
    }
    return null;
  }

}
