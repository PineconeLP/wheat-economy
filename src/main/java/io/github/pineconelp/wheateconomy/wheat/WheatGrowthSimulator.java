package io.github.pineconelp.wheateconomy.wheat;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class WheatGrowthSimulator {
  private final double CURRENT_WHEAT_GROWTH_CHANCE = 0.75;

  private final double OFFLINE_WHEAT_GROWTH_PROBABILITY_PER_RANDOM_TICK = 0.05;
  private final double SECONDS_PER_RANDOM_TICK = 68.27;

  private final boolean offlineGrowthEnabled;
  private final Random random;
  private NamespacedKey chunkUnloadTimestampKey;

  public WheatGrowthSimulator(Plugin plugin, boolean offlineGrowthEnabled) {
    this.offlineGrowthEnabled = offlineGrowthEnabled;
    this.random = new Random();
    this.chunkUnloadTimestampKey = new NamespacedKey(plugin, "chunk-unload-timestamp");
  }

  public void load(Chunk chunk) {
    if (!isOfflineGrowthEnabled()) {
      return;
    }

    PersistentDataContainer persistentDataContainer = chunk.getPersistentDataContainer();

    if (!persistentDataContainer.has(chunkUnloadTimestampKey)) {
      return;
    }

    long chunkUnloadTimestamp = persistentDataContainer.get(
        chunkUnloadTimestampKey,
        PersistentDataType.LONG);

    Instant currentTime = Instant.now();
    Instant chunkUnloadTime = Instant.ofEpochMilli(chunkUnloadTimestamp);
    Duration timeSinceChunkUnload = Duration.between(chunkUnloadTime, currentTime);

    simulate(chunk, timeSinceChunkUnload);
  }

  public void unload(Chunk chunk) {
    PersistentDataContainer persistentDataContainer = chunk.getPersistentDataContainer();

    persistentDataContainer.set(
        chunkUnloadTimestampKey,
        PersistentDataType.LONG,
        Instant.now().toEpochMilli());
  }

  public void simulate(Chunk chunk, Duration duration) {
    int randomTicks = (int) (duration.getSeconds() / SECONDS_PER_RANDOM_TICK);

    if (randomTicks <= 0) {
      return;
    }

    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        int blockX = chunk.getX() * 16 + x;
        int blockZ = chunk.getZ() * 16 + z;

        Block farmland = chunk.getWorld().getHighestBlockAt(blockX, blockZ);

        if (farmland.getType() != Material.FARMLAND) {
          continue;
        }

        Block wheatBlock = farmland.getRelative(BlockFace.UP);

        if (wheatBlock.getType() != Material.WHEAT) {
          continue;
        }

        if (!isWithinGrowthBoundaries(wheatBlock)) {
          continue;
        }

        Ageable wheat = (Ageable) wheatBlock.getBlockData();
        int age = wheat.getAge();

        for (int tick = 0; tick < randomTicks; tick++) {
          if (age >= wheat.getMaximumAge()) {
            break;
          }

          boolean isGrowthTick = random.nextDouble() < OFFLINE_WHEAT_GROWTH_PROBABILITY_PER_RANDOM_TICK;

          if (!isGrowthTick) {
            continue;
          }

          Ageable nextWheat = (Ageable) wheat.clone();
          nextWheat.setAge(age + 1);

          BlockState nextState = wheatBlock.getState();
          nextState.setBlockData(nextWheat);

          BlockGrowEvent growEvent = new BlockGrowEvent(wheatBlock, nextState);
          Bukkit.getPluginManager().callEvent(growEvent);

          if (!growEvent.isCancelled()) {
            age++;
          }
        }

        wheat.setAge(age);
        wheatBlock.setBlockData(wheat, false);
      }
    }
  }

  public boolean isWithinGrowthBoundaries(Block block) {
    if (block == null) {
      return false;
    }

    int y = block.getY();

    if (y < 60 || y > 160) {
      return false;
    }

    if (block.getLightFromSky() < 15) {
      return false;
    }

    return true;
  }

  public boolean shouldAdvanceWheatGrowth() {
    return random.nextDouble() < CURRENT_WHEAT_GROWTH_CHANCE;
  }

  public boolean isOfflineGrowthEnabled() {
    return this.offlineGrowthEnabled;
  }
}
