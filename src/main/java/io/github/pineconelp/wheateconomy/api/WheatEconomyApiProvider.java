package io.github.pineconelp.wheateconomy.api;

import org.bukkit.entity.Player;

import io.github.pineconelp.wheateconomy.bank.Bank;
import io.github.pineconelp.wheateconomy.bank.BankLeaderboard;

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
}
