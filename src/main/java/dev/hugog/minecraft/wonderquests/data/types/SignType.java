package dev.hugog.minecraft.wonderquests.data.types;

public enum SignType {
  ACTIVE_QUEST;

  public static SignType fromString(String string) {
    for (SignType signType : SignType.values()) {
      if (signType.name().equalsIgnoreCase(string)) {
        return signType;
      }
    }
    return null;
  }

}
