package io.github.pineconelp.wheateconomy.bank.withdraw;

import org.bukkit.entity.Player;

public interface WithdrawStrategy {
  boolean isValidAmount();

  boolean canWithdraw(int balance, int availableWheatSpace, int availableHayBaleSpace);

  void sendCannotWithdrawMessage(Player player, int balance, int availableWheatSpace, int availableHayBaleSpace);

  int calculateWheatToDeduct(int balance, int availableWheatSpace, int availableHayBaleSpace);

  void giveItemsToPlayer(Player player, int wheatDeducted);

  void sendSuccessMessage(Player player, int wheatDeducted);
}
