package io.github.pineconelp.wheateconomy.bank.withdraw;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class WithdrawHayBaleAmountStrategy implements WithdrawStrategy {
  private int amount;

  public WithdrawHayBaleAmountStrategy(int amount) {
    super();

    this.amount = amount;
  }

  @Override
  public boolean isValidAmount() {
    return amount > 0;
  }

  @Override
  public boolean canWithdraw(int balance, int availableWheatSpace, int availableHayBaleSpace) {
    return balance >= amount * 9 && availableHayBaleSpace >= amount;
  }

  @Override
  public void sendCannotWithdrawMessage(Player player, int balance, int availableWheatSpace, int availableHayBaleSpace) {
    if (balance < amount * 9) {
      player.sendMessage(Component.text(
          "Unable to withdraw " + amount + " hay bale(s). You only have " + balance + " wheat in your account. ("
              + (amount * 9) + " wheat required)",
          NamedTextColor.RED));
    } else {
      player.sendMessage(Component.text(
          "Unable to withdraw " + amount + " hay bale(s). You don't have enough space in your inventory.",
          NamedTextColor.RED));
    }
  }

  @Override
  public int calculateWheatToDeduct(int balance, int availableWheatSpace, int availableHayBaleSpace) {
    return amount * 9;
  }

  @Override
  public void giveItemsToPlayer(Player player, int wheatDeducted) {
    player.getInventory().addItem(new ItemStack(Material.HAY_BLOCK, wheatDeducted / 9))
        .values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
  }

  @Override
  public void sendSuccessMessage(Player player, int wheatDeducted) {
    int hayBales = wheatDeducted / 9;
    player.sendMessage(Component.text(
        "Successfully withdrew " + hayBales + " hay bale(s)! (" + wheatDeducted + " wheat)",
        NamedTextColor.GREEN));
  }
}
