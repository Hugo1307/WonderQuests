package dev.hugog.minecraft.wonderquests.data.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.hugog.minecraft.wonderquests.data.dtos.SignDto;
import dev.hugog.minecraft.wonderquests.data.models.SignModel;
import dev.hugog.minecraft.wonderquests.data.repositories.SignsRepository;
import dev.hugog.minecraft.wonderquests.data.types.SignType;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SignServiceTest {

  @Mock
  private SignsRepository signsRepository;

  @Mock
  private Location location;

  @Mock
  private World world;

  @InjectMocks
  private SignService signService;

  private SignDto signDto;

  @BeforeEach
  public void setUp() {
    signDto = new SignDto(
        1,
        SignType.ACTIVE_QUEST,
        "world",
        1,
        1,
        1
    );
  }

  @Test
  public void registerSignSuccessfully() {

    when(signsRepository.insert(any(SignModel.class)))
        .thenReturn(CompletableFuture.completedFuture(1));

    when(location.getWorld()).thenReturn(world);
    when(world.getName()).thenReturn("world");
    when(location.getBlockX()).thenReturn(1);
    when(location.getBlockY()).thenReturn(1);
    when(location.getBlockZ()).thenReturn(1);

    CompletableFuture<Integer> result = signService.registerSign(SignType.ACTIVE_QUEST, location);

    assertEquals(1, result.join());

  }

  @Test
  public void registerSignFails() {

    when(signsRepository.insert(any(SignModel.class)))
        .thenReturn(CompletableFuture.completedFuture(null));
    when(location.getWorld()).thenReturn(world);
    when(world.getName()).thenReturn("world");
    when(location.getBlockX()).thenReturn(1);
    when(location.getBlockY()).thenReturn(1);
    when(location.getBlockZ()).thenReturn(1);

    CompletableFuture<Integer> result = signService.registerSign(SignType.ACTIVE_QUEST, location);

    assertNull(result.join());

  }

  @Test
  public void unregisterSignSuccessfully() {

    when(signsRepository.deleteByLocation(
        any(String.class),
        any(Integer.class),
        any(Integer.class),
        any(Integer.class))
    ).thenReturn(CompletableFuture.completedFuture(1));

    when(location.getWorld()).thenReturn(world);
    when(world.getName()).thenReturn("world");
    when(location.getBlockX()).thenReturn(1);
    when(location.getBlockY()).thenReturn(1);
    when(location.getBlockZ()).thenReturn(1);

    CompletableFuture<Integer> result = signService.unregisterSign(location);

    assertEquals(1, result.join());

  }

  @Test
  public void unregisterSignFails() {

    when(signsRepository.deleteByLocation(
        any(String.class),
        any(Integer.class),
        any(Integer.class),
        any(Integer.class))
    ).thenReturn(CompletableFuture.completedFuture(null));

    when(location.getWorld()).thenReturn(world);
    when(world.getName()).thenReturn("world");
    when(location.getBlockX()).thenReturn(1);
    when(location.getBlockY()).thenReturn(1);
    when(location.getBlockZ()).thenReturn(1);

    CompletableFuture<Integer> result = signService.unregisterSign(location);

    assertNull(result.join());
  }

  @Test
  public void getAllSignsReturnsSigns() {

    Set<SignModel> signs = new HashSet<>();

    signs.add(signDto.toModel());

    when(signsRepository.findAll()).thenReturn(CompletableFuture.completedFuture(signs));

    CompletableFuture<Set<SignDto>> result = signService.getAllSigns();

    assertFalse(result.join().isEmpty());
    assertEquals(signDto, result.join().iterator().next());

  }

  @Test
  public void getAllSignsReturnsEmpty() {

    when(signsRepository.findAll()).thenReturn(CompletableFuture.completedFuture(new HashSet<>()));

    CompletableFuture<Set<SignDto>> result = signService.getAllSigns();

    assertTrue(result.join().isEmpty());

  }
}