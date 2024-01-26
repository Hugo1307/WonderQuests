package dev.hugog.minecraft.wonderquests.chat.interaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InteractiveSessionBuilderTest {

  @Mock
  private InteractiveSessionManager interactiveSessionManager;
  @Mock
  private Player targetPlayer;
  @Mock
  private InteractiveSessionFormatter interactiveSessionFormatter;
  @Mock
  private Runnable onSessionEnd;

  private InteractiveSessionBuilder interactiveSessionBuilder;

  @BeforeEach
  void setUp() {
    interactiveSessionBuilder = new InteractiveSessionBuilder(targetPlayer, interactiveSessionManager);
  }

  @Test
  @DisplayName("build() successfully builds an interactive session")
  public void build_SuccessfullyBuildsInteractiveSession() {

    InteractiveStep step1 = mock(InteractiveStep.class);
    InteractiveStep step2 = mock(InteractiveStep.class);
    List<InteractiveStep> steps = Arrays.asList(step1, step2);

    InteractiveSession interactiveSession = interactiveSessionBuilder
        .withSteps(steps)
        .withSessionFormatter(interactiveSessionFormatter)
        .withSessionEndCallback(onSessionEnd)
        .build();

    assertNotNull(interactiveSession);
    assertEquals(targetPlayer, interactiveSession.getTargetPlayer());
    assertEquals(interactiveSessionManager, interactiveSession.getInteractiveSessionManager());
    assertEquals(steps, interactiveSession.getInteractionSteps());
    assertEquals(interactiveSessionFormatter, interactiveSession.getInteractiveSessionFormatter());
    assertEquals(onSessionEnd, interactiveSession.getOnSessionEnd());

  }

}