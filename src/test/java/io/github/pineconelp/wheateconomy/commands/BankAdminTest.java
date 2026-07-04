package io.github.pineconelp.wheateconomy.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import io.github.pineconelp.wheateconomy.bank.LedgerEntryType;
import io.github.pineconelp.wheateconomy.support.EconomyTest;

class BankAdminTest extends EconomyTest {

  @Test
  void set_recordsALedgerEntryWithTheDelta() throws Exception {
    PlayerMock admin = server.addPlayer("Admin");
    admin.setOp(true);
    UUID targetId = server.getOfflinePlayer("Bob").getUniqueId();
    seedBalance(targetId, 5);

    admin.performCommand("bank set Bob 100");
    drainScheduler();

    List<LedgerEntry> ledger = ledgerOf(targetId);
    assertEquals(1, ledger.size());
    assertEquals(LedgerEntryType.ADMIN_SET.name(), ledger.get(0).type());
    assertEquals(95, ledger.get(0).balanceChange());
    assertEquals(100, ledger.get(0).balanceAfter());
  }

  @Test
  void add_recordsALedgerEntry() throws Exception {
    PlayerMock admin = server.addPlayer("Admin");
    admin.setOp(true);
    UUID targetId = server.getOfflinePlayer("Bob").getUniqueId();
    seedBalance(targetId, 40);

    admin.performCommand("bank add Bob 10");
    drainScheduler();

    List<LedgerEntry> ledger = ledgerOf(targetId);
    assertEquals(1, ledger.size());
    assertEquals(LedgerEntryType.ADMIN_ADD.name(), ledger.get(0).type());
    assertEquals(10, ledger.get(0).balanceChange());
    assertEquals(50, ledger.get(0).balanceAfter());
  }

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
