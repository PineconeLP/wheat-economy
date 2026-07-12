package io.github.pineconelp.wheateconomy.worldguard;

import org.bukkit.block.Block;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

public class WorldGuardWheatGrowthRegion {
  public static final StateFlag WHEAT_GROWTH_FLAG = new StateFlag("wheat-growth", true);

  public void registerFlag() {
    FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

    try {
      registry.register(WHEAT_GROWTH_FLAG);
    } catch (FlagConflictException e) {
      // Flag is already registered (e.g. after a reload); nothing to do.
    }
  }

  public boolean isWheatGrowthDenied(Block block) {
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionQuery query = container.createQuery();

    Location location = BukkitAdapter.adapt(block.getLocation());

    return query.queryState(location, null, WHEAT_GROWTH_FLAG) == StateFlag.State.DENY;
  }
}
