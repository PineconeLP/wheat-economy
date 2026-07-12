package io.github.pineconelp.wheateconomy.api;

import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.flags.StateFlag;

import io.github.pineconelp.wheateconomy.bank.Bank;
import io.github.pineconelp.wheateconomy.bank.BankLeaderboard;
import io.github.pineconelp.wheateconomy.worldguard.WorldGuardWheatGrowthRegion;

public class WheatEconomyApiProvider implements WheatEconomyApi {
  private final Bank bank;
  private final BankLeaderboard bankLeaderboard;

  public WheatEconomyApiProvider(Bank bank, BankLeaderboard bankLeaderboard) {
    this.bank = bank;
    this.bankLeaderboard = bankLeaderboard;
  }

  @Override
  public void openBankSummary(Player player) {
    bank.showSummary(player);
  }

  @Override
  public void openBankLeaderboard(Player player) {
    bankLeaderboard.open(player);
  }

  @Override
  public StateFlag getWheatGrowthFlag() {
    return WorldGuardWheatGrowthRegion.WHEAT_GROWTH_FLAG;
  }
}
