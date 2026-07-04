package io.github.pineconelp.wheateconomy.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import io.github.pineconelp.wheateconomy.support.EconomyTest;

class BankBalanceTest extends EconomyTest {

  @Test
  void balance_showsTheStoredBalanceToThePlayer() throws Exception {
    PlayerMock player = addPlayer();
    seedBalance(player.getUniqueId(), 42);

    player.performCommand("bank balance");
    drainScheduler();

    boolean sawBalance = false;
    String message;
    while ((message = player.nextMessage()) != null) {
      if (message.contains("42")) {
        sawBalance = true;
        break;
      }
    }

    assertTrue(sawBalance);
  }
}
