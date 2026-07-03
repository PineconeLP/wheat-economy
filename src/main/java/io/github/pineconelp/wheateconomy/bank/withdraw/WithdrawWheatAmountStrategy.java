package io.github.pineconelp.wheateconomy.bank.withdraw;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class WithdrawWheatAmountStrategy implements WithdrawStrategy {
  private int amount;

  public WithdrawWheatAmountStrategy(int amount) {
    super();

    this.amount = amount;
  }

  @Override
  public boolean isValidAmount() {
    return amount > 0;
  }

  @Override
  public boolean canWithdraw(int balance, int availableWheatSpace, int availableHayBaleSpace) {
    return balance >= amount && availableWheatSpace >= amount;
  }

  @Override
  public void sendCannotWithdrawMessage(Player player, int balance, int availableWheatSpace, int availableHayBaleSpace) {
    if (balance < amount) {
      player.sendMessage(Component.text(
          "Unable to withdraw " + amount + " wheat. You only have " + balance + " wheat in your account.",
          NamedTextColor.RED));
    } else {
      player.sendMessage(Component.text(
          "Unable to withdraw " + amount + " wheat. You don't have enough space in your inventory.",
          NamedTextColor.RED));
    }
  }

  @Override
  public int calculateWheatToDeduct(int balance, int availableWheatSpace, int availableHayBaleSpace) {
    return amount;
  }

  @Override
  public void giveItemsToPlayer(Player player, int wheatDeducted) {
    player.getInventory().addItem(new ItemStack(Material.WHEAT, wheatDeducted))
        .values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
  }

  @Override
  public void sendSuccessMessage(Player player, int wheatDeducted) {
    player.sendMessage(Component.text("Successfully withdrew " + wheatDeducted + " wheat!", NamedTextColor.GREEN));
  }
}
