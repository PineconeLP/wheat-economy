package io.github.pineconelp.wheateconomy.bank.deposit;

import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DepositAllHayBalesStrategy implements DepositStrategy {
  @Override
  public boolean isValidAmount() {
    return true;
  }

  @Override
  public boolean isSufficientAmount(int playerHayBaleAmount, int playerWheatAmount) {
    return true;
  }

  @Override
  public void sendInsufficientAmountMessage(Player player, int playerHayBaleAmount, int playerWheatAmount) {
  }

  @Override
  public int calculateHayBaleAmountToTake(int playerHayBaleAmount) {
    return playerHayBaleAmount;
  }

  @Override
  public int calculateWheatAmountToTake(int playerWheatAmount) {
    return 0;
  }

  @Override
  public int calculateAmountToDeposit(int playerHayBaleAmount, int playerWheatAmount) {
    return playerHayBaleAmount * 9;
  }

  @Override
  public void sendDepositSuccessMessage(Player player, int playerHayBaleAmount, int playerWheatAmount) {
    player.sendMessage(
        Component.text(
            "Successfully deposited " + calculateAmountToDeposit(playerHayBaleAmount, playerWheatAmount) + " wheat! ("
                + playerHayBaleAmount + " Hay Bales)",
            NamedTextColor.GREEN));
  }
}
