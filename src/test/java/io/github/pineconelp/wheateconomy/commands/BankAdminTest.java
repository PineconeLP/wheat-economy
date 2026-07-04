package io.github.pineconelp.wheateconomy.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import io.github.pineconelp.wheateconomy.support.EconomyTest;

class BankAdminTest extends EconomyTest {

  @Test
  void set_overwritesTheTargetBalance() throws Exception {
    PlayerMock admin = server.addPlayer("Admin");
    admin.setOp(true);
    UUID targetId = server.getOfflinePlayer("Bob").getUniqueId();
    seedBalance(targetId, 5);

    admin.performCommand("bank set Bob 100");
    drainScheduler();

    assertEquals(100, balanceOf(targetId));
  }

  @Test
  void add_increasesTheTargetBalance() throws Exception {
    PlayerMock admin = server.addPlayer("Admin");
    admin.setOp(true);
    UUID targetId = server.getOfflinePlayer("Bob").getUniqueId();
    seedBalance(targetId, 40);

    admin.performCommand("bank add Bob 10");
    drainScheduler();

    assertEquals(50, balanceOf(targetId));
  }

  @Test
  void adminCommand_withoutPermission_changesNothing() throws Exception {
    PlayerMock player = server.addPlayer("Nobody");
    UUID targetId = server.getOfflinePlayer("Bob").getUniqueId();
    seedBalance(targetId, 40);

    player.performCommand("bank set Bob 100");
    drainScheduler();

    assertEquals(40, balanceOf(targetId));
  }
}
