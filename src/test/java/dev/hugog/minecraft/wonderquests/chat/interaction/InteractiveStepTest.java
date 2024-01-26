package dev.hugog.minecraft.wonderquests.chat.interaction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InteractiveStepTest {

  @Mock
  private Player targetPlayer;
  @Mock
  private Predicate<String> inputVerification;
  @Mock
  private Consumer<String> onValidInput;
  @Mock
  private Consumer<String> onInvalidInput;
  @Mock
  private Function<String, String> customNextStep;

  private InteractiveStep interactiveStep;

  @BeforeEach
  void setUp() {

    interactiveStep = InteractiveStep.builder()
        .id("step1")
        .message(Component.text("Test Message"))
        .hint(Component.text("Test Hint"))
        .inputVerification(inputVerification)
        .onValidInput(onValidInput)
        .onInvalidInput(onInvalidInput)
        .customNextStep(customNextStep)
        .isTerminalStep(false)
        .formatter(null)
        .build();

  }

  @Test
  @DisplayName("run() successfully sends the step message to the player")
  public void run_SuccessfullySendsStepMessageToPlayer() {

    interactiveStep.run(targetPlayer);
    verify(targetPlayer).sendMessage(Component.text("Test Message"));

  }

  @Test
  @DisplayName("submitStep() successfully processes valid input")
  public void submitStep_ProcessesValidInput() {

    when(inputVerification.test("valid"))
        .thenReturn(true);
    assertTrue(interactiveStep.submitStep("valid"));
    verify(onValidInput).accept("valid");

  }

  @Test
  @DisplayName("submitStep() successfully processes invalid input")
  public void submitStep_ProcessesInvalidInput() {

    when(inputVerification.test("invalid"))
        .thenReturn(false);
    assertFalse(interactiveStep.submitStep("invalid"));
    verify(onInvalidInput).accept("invalid");

  }

}