package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.dtos.SignDto;
import dev.hugog.minecraft.wonderquests.data.repositories.SignsRepository;
import dev.hugog.minecraft.wonderquests.data.types.SignType;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;

public class SignService {

  private final SignsRepository signRepository;

  @Inject
  public SignService(SignsRepository signRepository) {
    this.signRepository = signRepository;
  }

  public CompletableFuture<Integer> registerSign(SignType signType, Location location) {

    SignDto signDto = SignDto.createSign(signType, location);
    return signRepository.insert(signDto.toModel());

  }

  public CompletableFuture<Integer> unregisterSign(Location location) {
    return signRepository.deleteByLocation(
        location.getWorld().getName(),
        location.getBlockX(),
        location.getBlockY(),
        location.getBlockZ()
    );
  }

}
