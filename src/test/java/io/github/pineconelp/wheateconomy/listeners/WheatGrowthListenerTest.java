package io.github.pineconelp.wheateconomy.listeners;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import io.github.pineconelp.wheateconomy.support.EconomyTest;

class WheatGrowthListenerTest extends EconomyTest {
  @Test
  void naturalWheatGrowth_belowMinimumHeight_isCancelled() {
    Block wheat = world().getBlockAt(0, 59, 0);
    wheat.setType(Material.WHEAT);

    BlockGrowEvent event = new BlockGrowEvent(wheat, wheat.getState());
    server.getPluginManager().callEvent(event);

    assertTrue(event.isCancelled());
  }

  @Test
  void naturalGrowth_forOtherCropsIsNotManagedByWheatEconomy() {
    Block carrots = world().getBlockAt(0, 59, 0);
    carrots.setType(Material.CARROTS);

    BlockGrowEvent event = new BlockGrowEvent(carrots, carrots.getState());
    server.getPluginManager().callEvent(event);

    assertFalse(event.isCancelled());
  }

  @Test
  void bonemealOnWheat_byRegularPlayerIsCancelled() {
    PlayerMock player = addPlayer();
    Block wheat = world().getBlockAt(0, 80, 0);
    wheat.setType(Material.WHEAT);

    BlockFertilizeEvent event = new BlockFertilizeEvent(wheat, player, List.of(wheat.getState()));
    server.getPluginManager().callEvent(event);

    assertTrue(event.isCancelled());
  }

  @Test
  void bonemealOnWheat_byOperatorIsAllowed() {
    PlayerMock player = addPlayer();
    player.setOp(true);
    Block wheat = world().getBlockAt(0, 80, 0);
    wheat.setType(Material.WHEAT);

    BlockFertilizeEvent event = new BlockFertilizeEvent(wheat, player, List.of(wheat.getState()));
    server.getPluginManager().callEvent(event);

    assertFalse(event.isCancelled());
  }
}
