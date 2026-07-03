package io.github.pineconelp.wheateconomy.bank.deposit;

import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DepositWheatAmountStrategy implements DepositStrategy {
  private int amount;

  public DepositWheatAmountStrategy(int amount) {
    super();

    this.amount = amount;
  }

  @Override
  public boolean isValidAmount() {
    return amount > 0;
  }

  @Override
  public boolean isSufficientAmount(int playerHayBaleAmount, int playerWheatAmount) {
    return playerWheatAmount >= amount;
  }

  @Override
  public void sendInsufficientAmountMessage(Player player, int playerHayBaleAmount, int playerWheatAmount) {
    player.sendMessage(
        Component.text("Unable to deposit " + amount + " wheat. You only have " + playerWheatAmount
            + " wheat in your inventory.", NamedTextColor.RED));
  }

  @Override
  public int calculateHayBaleAmountToTake(int playerHayBaleAmount) {
    return 0;
  }

  @Override
  public int calculateWheatAmountToTake(int playerWheatAmount) {
    return amount;
  }

  @Override
  public int calculateAmountToDeposit(int playerHayBaleAmount, int playerWheatAmount) {
    return amount;
  }

  @Override
  public void sendDepositSuccessMessage(Player player, int playerHayBaleAmount, int playerWheatAmount) {
    player.sendMessage(
        Component.text(
            "Successfully deposited " + calculateAmountToDeposit(playerHayBaleAmount, playerWheatAmount) + " wheat!",
            NamedTextColor.GREEN));
  }
}
