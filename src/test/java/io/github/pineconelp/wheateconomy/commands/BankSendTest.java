package io.github.pineconelp.wheateconomy.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import io.github.pineconelp.wheateconomy.support.EconomyTest;

class BankSendTest extends EconomyTest {

  @Test
  void send_transfersWheatBetweenAccounts() throws Exception {
    PlayerMock sender = server.addPlayer("Alice");
    PlayerMock target = server.addPlayer("Bob");
    seedBalance(sender.getUniqueId(), 50);

    sender.performCommand("bank send Bob 20");
    drainScheduler();

    assertEquals(30, balanceOf(sender.getUniqueId()));
    assertEquals(20, balanceOf(target.getUniqueId()));
  }

  @Test
  void send_withInsufficientBalance_transfersNothing() throws Exception {
    PlayerMock sender = server.addPlayer("Alice");
    PlayerMock target = server.addPlayer("Bob");
    seedBalance(sender.getUniqueId(), 5);

    sender.performCommand("bank send Bob 20");
    drainScheduler();

    assertEquals(5, balanceOf(sender.getUniqueId()));
    assertEquals(0, balanceOf(target.getUniqueId()));
  }

  @Test
  void send_toOfflineTarget_transfersNothing() throws Exception {
    PlayerMock sender = server.addPlayer("Alice");
    seedBalance(sender.getUniqueId(), 50);

    sender.performCommand("bank send Ghost 20");
    drainScheduler();

    assertEquals(50, balanceOf(sender.getUniqueId()));
  }
}
