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

/**
 * This class provides services for managing signs in the game.
 */
public class SignService {

  private final SignsRepository signRepository;

  /**
   * Constructor for the SignService class.
   *
   * @param signRepository The repository instance used for database operations related to signs.
   */
  @Inject
  public SignService(SignsRepository signRepository) {
    this.signRepository = signRepository;
  }

  /**
   * This method registers a new sign in the game.
   *
   * @param signType The type of the sign.
   * @param location The location of the sign.
   * @return a CompletableFuture that will be completed with the id of the registered sign.
   */
  public CompletableFuture<Integer> registerSign(SignType signType, Location location) {

    SignDto signDto = SignDto.createSign(signType, location);
    return signRepository.insert(signDto.toModel());

  }

  /**
   * This method unregisters a sign in the game.
   *
   * @param location The location of the sign.
   * @return a CompletableFuture that will be completed with the id of the unregistered sign.
   */
  public CompletableFuture<Integer> unregisterSign(Location location) {
    return signRepository.deleteByLocation(
        location.getWorld().getName(),
        location.getBlockX(),
        location.getBlockY(),
        location.getBlockZ()
    );
  }

  /**
   * This method retrieves all signs in the game.
   *
   * @return a CompletableFuture that will be completed with a Set containing all signs in the game.
   */
  public CompletableFuture<Set<SignDto>> getAllSigns() {
    return signRepository.findAll()
        .thenApply(signs -> signs.stream().map(SignModel::toDto).collect(Collectors.toSet()));
  }

}