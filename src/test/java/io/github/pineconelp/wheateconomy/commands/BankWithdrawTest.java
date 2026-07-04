package io.github.pineconelp.wheateconomy.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import io.github.pineconelp.wheateconomy.bank.LedgerEntryType;
import io.github.pineconelp.wheateconomy.support.EconomyTest;

class BankWithdrawTest extends EconomyTest {

  @Test
  void withdraw_recordsALedgerEntry() throws Exception {
    PlayerMock player = addPlayer();
    seedBalance(player.getUniqueId(), 20);

    player.performCommand("bank withdraw wheat 10");
    drainScheduler();

    List<LedgerEntry> ledger = ledgerOf(player.getUniqueId());
    assertEquals(1, ledger.size());
    assertEquals(LedgerEntryType.WITHDRAW.name(), ledger.get(0).type());
    assertEquals(-10, ledger.get(0).balanceChange());
    assertEquals(10, ledger.get(0).balanceAfter());
  }

  @Test
  void withdraw_withInsufficientBalance_recordsNoLedgerEntry() throws Exception {
    PlayerMock player = addPlayer();
    seedBalance(player.getUniqueId(), 5);

    player.performCommand("bank withdraw wheat 10");
    drainScheduler();

    assertTrue(ledgerOf(player.getUniqueId()).isEmpty());
  }

  @Test
  void withdrawWheatAmount_deductsBalanceAndGivesWheat() throws Exception {
    PlayerMock player = addPlayer();
    seedBalance(player.getUniqueId(), 20);

    player.performCommand("bank withdraw wheat 10");
    drainScheduler();

    assertEquals(10, balanceOf(player.getUniqueId()));
    assertEquals(10, countInInventory(player, Material.WHEAT));
  }

  @Test
  void withdrawWheatAll_emptiesTheAccountIntoTheInventory() throws Exception {
    PlayerMock player = addPlayer();
    seedBalance(player.getUniqueId(), 30);

    player.performCommand("bank withdraw wheat all");
    drainScheduler();

    assertEquals(0, balanceOf(player.getUniqueId()));
    assertEquals(30, countInInventory(player, Material.WHEAT));
  }

  @Test
  void withdrawHayBaleAmount_convertsNineWheatIntoOneHayBale() throws Exception {
    PlayerMock player = addPlayer();
    seedBalance(player.getUniqueId(), 20);

    player.performCommand("bank withdraw haybale 2");
    drainScheduler();

    assertEquals(2, balanceOf(player.getUniqueId()));
    assertEquals(2, countInInventory(player, Material.HAY_BLOCK));
  }

  @Test
  void withdrawWheatAmount_withInsufficientBalance_changesNothing() throws Exception {
    PlayerMock player = addPlayer();
    seedBalance(player.getUniqueId(), 5);

    player.performCommand("bank withdraw wheat 10");
    drainScheduler();

    assertEquals(5, balanceOf(player.getUniqueId()));
    assertEquals(0, countInInventory(player, Material.WHEAT));
  }
}
