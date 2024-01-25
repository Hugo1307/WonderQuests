package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.dtos.SignDto;
import dev.hugog.minecraft.wonderquests.data.models.SignModel;
import dev.hugog.minecraft.wonderquests.data.repositories.SignsRepository;
import dev.hugog.minecraft.wonderquests.data.types.SignType;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
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

  public CompletableFuture<Set<SignDto>> getAllSigns() {
    return signRepository.findAll()
        .thenApply(signs -> signs.stream().map(SignModel::toDto).collect(Collectors.toSet()));
  }

}
