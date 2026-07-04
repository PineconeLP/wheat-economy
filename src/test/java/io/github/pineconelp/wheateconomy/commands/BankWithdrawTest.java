package io.github.pineconelp.wheateconomy.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import io.github.pineconelp.wheateconomy.support.EconomyTest;

class BankWithdrawTest extends EconomyTest {

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
