package io.github.pineconelp.wheateconomy.api;

import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.flags.StateFlag;

public interface WheatEconomyApi {
  void openBankSummary(Player player);

  void openBankLeaderboard(Player player);

  StateFlag getWheatGrowthFlag();
}
