package io.github.pineconelp.wheateconomy.bank.deposit;

import org.bukkit.entity.Player;

public interface DepositStrategy {
  boolean isValidAmount();

  boolean isSufficientAmount(int playerHayBaleAmount, int playerWheatAmount);

  void sendInsufficientAmountMessage(Player player, int playerHayBaleAmount, int playerWheatAmount);

  int calculateHayBaleAmountToTake(int playerHayBaleAmount);

  int calculateWheatAmountToTake(int playerWheatAmount);

  int calculateAmountToDeposit(int playerHayBaleAmount, int playerWheatAmount);

  void sendDepositSuccessMessage(Player player, int playerHayBaleAmount, int playerWheatAmount);
}
