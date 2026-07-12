package io.github.pineconelp.wheateconomy.worldguard;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import io.github.pineconelp.wheateconomy.bank.BankerLocationPolicy;

public class WorldGuardBankerRegion implements BankerLocationPolicy {
  public static final StateFlag HAS_BANKER_FLAG = new StateFlag("has-banker", false);

  public void registerFlag() {
    FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

    try {
      registry.register(HAS_BANKER_FLAG);
    } catch (FlagConflictException e) {
      // Flag is already registered (e.g. after a reload); nothing to do.
    }
  }

  @Override
  public boolean isNearBanker(Player player) {
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionQuery query = container.createQuery();

    com.sk89q.worldedit.util.Location location = BukkitAdapter.adapt(player.getLocation());

    return query.testState(location, WorldGuardPlugin.inst().wrapPlayer(player), HAS_BANKER_FLAG);
  }
}
