package io.github.pineconelp.wheateconomy.bank.deposit;

import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DepositHayBaleAmountStrategy implements DepositStrategy {
  private int amount;

  public DepositHayBaleAmountStrategy(int amount) {
    super();

    this.amount = amount;
  }

  @Override
  public boolean isValidAmount() {
    return amount > 0;
  }

  @Override
  public boolean isSufficientAmount(int playerHayBaleAmount, int playerWheatAmount) {
    return playerHayBaleAmount >= amount;
  }

  @Override
  public void sendInsufficientAmountMessage(Player player, int playerHayBaleAmount, int playerWheatAmount) {
    player.sendMessage(
        Component.text("Unable to deposit " + amount + " hay bale(s). You only have " + playerHayBaleAmount
            + " hay bale(s) in your inventory.", NamedTextColor.RED));
  }

  @Override
  public int calculateHayBaleAmountToTake(int playerHayBaleAmount) {
    return amount;
  }

  @Override
  public int calculateWheatAmountToTake(int playerWheatAmount) {
    return 0;
  }

  @Override
  public int calculateAmountToDeposit(int playerHayBaleAmount, int playerWheatAmount) {
    return amount * 9;
  }

  @Override
  public void sendDepositSuccessMessage(Player player, int playerHayBaleAmount, int playerWheatAmount) {
    player.sendMessage(
        Component.text(
            "Successfully deposited " + calculateAmountToDeposit(playerHayBaleAmount, playerWheatAmount) + " wheat! ("
                + amount + " Hay Bales)",
            NamedTextColor.GREEN));
  }
}
