package dev.hugog.minecraft.wonderquests.data.types;

public enum RewardType {

  MONEY, ITEMS, EXPERIENCE, COMMAND;

  public static RewardType fromString(String string) {
    for (RewardType rewardType : RewardType.values()) {
      if (rewardType.name().equalsIgnoreCase(string)) {
        return rewardType;
      }
    }
    return null;
  }

}
