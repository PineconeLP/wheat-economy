package io.github.pineconelp.wheateconomy.listeners;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import io.github.pineconelp.wheateconomy.support.EconomyTest;

class BankTransactionListenerTest extends EconomyTest {
  @Test
  void pickupItem_isCancelledWhilePlayerHasBankTransactionInProgress() {
    Set<UUID> transactingPlayerIds = ConcurrentHashMap.newKeySet();
    PlayerMock player = addPlayer();
    transactingPlayerIds.add(player.getUniqueId());
    server.getPluginManager().registerEvents(new BankTransactionListener(transactingPlayerIds), plugin);
    Item item = world().dropItemNaturally(player.getLocation(), new ItemStack(Material.WHEAT, 1));

    EntityPickupItemEvent event = new EntityPickupItemEvent(player, item, 0);
    server.getPluginManager().callEvent(event);

    assertTrue(event.isCancelled());
  }

  @Test
  void dropItem_isAllowedWhenPlayerIsNotBanking() {
    Set<UUID> transactingPlayerIds = ConcurrentHashMap.newKeySet();
    PlayerMock player = addPlayer();
    server.getPluginManager().registerEvents(new BankTransactionListener(transactingPlayerIds), plugin);
    Item item = world().dropItemNaturally(player.getLocation(), new ItemStack(Material.WHEAT, 1));

    PlayerDropItemEvent event = new PlayerDropItemEvent(player, item);
    server.getPluginManager().callEvent(event);

    assertFalse(event.isCancelled());
  }

  @Test
  void dropItem_isCancelledWhilePlayerHasBankTransactionInProgress() {
    Set<UUID> transactingPlayerIds = ConcurrentHashMap.newKeySet();
    PlayerMock player = addPlayer();
    transactingPlayerIds.add(player.getUniqueId());
    server.getPluginManager().registerEvents(new BankTransactionListener(transactingPlayerIds), plugin);
    Item item = world().dropItemNaturally(player.getLocation(), new ItemStack(Material.WHEAT, 1));

    PlayerDropItemEvent event = new PlayerDropItemEvent(player, item);
    server.getPluginManager().callEvent(event);

    assertTrue(event.isCancelled());
  }
}
