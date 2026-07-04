package io.github.pineconelp.wheateconomy.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import io.github.pineconelp.wheateconomy.support.EconomyTest;

class BankDepositTest extends EconomyTest {

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
