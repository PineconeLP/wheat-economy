package io.github.pineconelp.wheateconomy.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import io.github.pineconelp.wheateconomy.bank.LedgerEntryType;
import io.github.pineconelp.wheateconomy.support.EconomyTest;

class BankDepositTest extends EconomyTest {

  @Test
  void deposit_recordsALedgerEntry() throws Exception {
    PlayerMock player = addPlayerWithItems(Material.WHEAT, 64);

    player.performCommand("bank deposit wheat 10");
    drainScheduler();

    List<LedgerEntry> ledger = ledgerOf(player.getUniqueId());
    assertEquals(1, ledger.size());
    assertEquals(LedgerEntryType.DEPOSIT.name(), ledger.get(0).type());
    assertEquals(10, ledger.get(0).balanceChange());
    assertEquals(10, ledger.get(0).balanceAfter());
    assertEquals(null, ledger.get(0).targetPlayerId());
  }

  @Test
  void depositAll_movesAllWheatFromInventoryToBank() throws Exception {
    PlayerMock player = addPlayerWithItems(Material.WHEAT, 64);

    player.performCommand("bank deposit all");
    drainScheduler();

    assertEquals(64, balanceOf(player.getUniqueId()));
    assertEquals(0, countInInventory(player, Material.WHEAT));
  }

  @Test
  void depositWheatAmount_depositsOnlyTheRequestedAmount() throws Exception {
    PlayerMock player = addPlayerWithItems(Material.WHEAT, 64);

    player.performCommand("bank deposit wheat 10");
    drainScheduler();

    assertEquals(10, balanceOf(player.getUniqueId()));
    assertEquals(54, countInInventory(player, Material.WHEAT));
  }

  @Test
  void depositHayBaleAmount_convertsHayBalesToNineWheatEach() throws Exception {
    PlayerMock player = addPlayerWithItems(Material.HAY_BLOCK, 5);

    player.performCommand("bank deposit haybale 2");
    drainScheduler();

    assertEquals(18, balanceOf(player.getUniqueId()));
    assertEquals(3, countInInventory(player, Material.HAY_BLOCK));
  }

  @Test
  void depositWheatAmount_withInsufficientWheat_depositsNothing() throws Exception {
    PlayerMock player = addPlayerWithItems(Material.WHEAT, 5);

    player.performCommand("bank deposit wheat 10");
    drainScheduler();

    assertEquals(0, balanceOf(player.getUniqueId()));
    assertEquals(5, countInInventory(player, Material.WHEAT));
  }
}
