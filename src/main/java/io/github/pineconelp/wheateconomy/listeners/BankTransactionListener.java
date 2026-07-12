package io.github.pineconelp.wheateconomy.listeners;

import java.util.Set;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class BankTransactionListener implements Listener {
  private final Set<UUID> transactingPlayerIds;

  public BankTransactionListener(Set<UUID> transactingPlayerIds) {
    this.transactingPlayerIds = transactingPlayerIds;
  }

  @EventHandler
  public void onPickupItemWhileBanking(EntityPickupItemEvent event) {
    if (transactingPlayerIds.contains(event.getEntity().getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onDropItemWhileBanking(PlayerDropItemEvent event) {
    if (transactingPlayerIds.contains(event.getPlayer().getUniqueId())) {
      event.setCancelled(true);
    }
  }
}
