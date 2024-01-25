package dev.hugog.minecraft.wonderquests.data.dtos;

import dev.hugog.minecraft.wonderquests.data.models.SignModel;
import dev.hugog.minecraft.wonderquests.data.types.SignType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SignDto implements Dto<SignModel> {

  private Integer id;
  private SignType type;
  private String worldName;
  private Integer x;
  private Integer y;
  private Integer z;

  public static SignDto createSign(SignType signType, Location location) {
    return new SignDto(null, signType, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
  }

  @Override
  public SignModel toModel() {
    return new SignModel(id, type.name(), worldName, x, y, z);
  }

}
