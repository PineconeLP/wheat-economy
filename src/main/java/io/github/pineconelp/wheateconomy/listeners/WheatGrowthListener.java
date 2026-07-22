package io.github.pineconelp.wheateconomy.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import io.github.pineconelp.wheateconomy.wheat.WheatGrowthSimulator;
import io.github.pineconelp.wheateconomy.worldguard.WorldGuardWheatGrowthRegion;

public class WheatGrowthListener implements Listener {
  private final WheatGrowthSimulator wheatGrowthSimulator;
  private final WorldGuardWheatGrowthRegion wheatGrowthRegion;

  public WheatGrowthListener(
      WheatGrowthSimulator wheatGrowthSimulator,
      WorldGuardWheatGrowthRegion wheatGrowthRegion) {
    super();

    this.wheatGrowthSimulator = wheatGrowthSimulator;
    this.wheatGrowthRegion = wheatGrowthRegion;
  }

  @EventHandler
  public void onChunkLoad(ChunkLoadEvent event) {
    wheatGrowthSimulator.load(event.getChunk());
  }

  @EventHandler
  public void onChunkUnload(ChunkUnloadEvent event) {
    wheatGrowthSimulator.unload(event.getChunk());
  }

  @EventHandler
  public void onWheatGrow(BlockGrowEvent event) {
    Block block = event.getBlock();

    if (block.getType() != Material.WHEAT) {
      return;
    }

    if (isWheatGrowthDenied(block)) {
      event.setCancelled(true);
      return;
    }

    if (!wheatGrowthSimulator.isWithinGrowthBoundaries(block)) {
      event.setCancelled(true);
      return;
    }

    if (!wheatGrowthSimulator.shouldAdvanceWheatGrowth()) {
      event.setCancelled(true);
      return;
    }
  }

  @EventHandler
  public void onBeeFertilize(EntityChangeBlockEvent event) {
    if (event.getEntityType() != EntityType.BEE) {
      return;
    }

    if (event.getTo() != Material.WHEAT) {
      return;
    }

    event.setCancelled(true);
  }

  @EventHandler
  public void onVillagerFarm(EntityChangeBlockEvent event) {
    if (event.getEntityType() != EntityType.VILLAGER) {
      return;
    }

    if (event.getTo() != Material.WHEAT) {
      return;
    }

    event.setCancelled(true);
  }

  private boolean isWheatGrowthDenied(Block block) {
    return wheatGrowthRegion != null && wheatGrowthRegion.isWheatGrowthDenied(block);
  }

  @EventHandler
  public void onFertilizeSeeds(BlockFertilizeEvent event) {
    Player player = event.getPlayer();

    if (player != null && player.isOp()) {
      return;
    }

    Material blockType = event.getBlock().getType();

    if (blockType != Material.WHEAT) {
      return;
    }

    event.setCancelled(true);
  }
}
