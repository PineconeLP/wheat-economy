package io.github.pineconelp.wheateconomy.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import io.github.pineconelp.wheateconomy.bank.LedgerEntryType;
import io.github.pineconelp.wheateconomy.support.EconomyTest;

class BankSendTest extends EconomyTest {

  @Test
  void send_recordsALedgerEntryForEachSide() throws Exception {
    PlayerMock sender = server.addPlayer("Alice");
    PlayerMock target = server.addPlayer("Bob");
    seedBalance(sender.getUniqueId(), 50);

    sender.performCommand("bank send Bob 20");
    drainScheduler();

    List<LedgerEntry> senderLedger = ledgerOf(sender.getUniqueId());
    assertEquals(1, senderLedger.size());
    assertEquals(LedgerEntryType.SEND.name(), senderLedger.get(0).type());
    assertEquals(-20, senderLedger.get(0).balanceChange());
    assertEquals(30, senderLedger.get(0).balanceAfter());
    assertEquals(target.getUniqueId(), senderLedger.get(0).targetPlayerId());

    List<LedgerEntry> targetLedger = ledgerOf(target.getUniqueId());
    assertEquals(1, targetLedger.size());
    assertEquals(LedgerEntryType.RECEIVE.name(), targetLedger.get(0).type());
    assertEquals(20, targetLedger.get(0).balanceChange());
    assertEquals(20, targetLedger.get(0).balanceAfter());
    assertEquals(sender.getUniqueId(), targetLedger.get(0).targetPlayerId());
  }

  @Test
  void send_withInsufficientBalance_recordsNoLedgerEntry() throws Exception {
    PlayerMock sender = server.addPlayer("Alice");
    PlayerMock target = server.addPlayer("Bob");
    seedBalance(sender.getUniqueId(), 5);

    sender.performCommand("bank send Bob 20");
    drainScheduler();

    assertTrue(ledgerOf(sender.getUniqueId()).isEmpty());
    assertTrue(ledgerOf(target.getUniqueId()).isEmpty());
  }

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
