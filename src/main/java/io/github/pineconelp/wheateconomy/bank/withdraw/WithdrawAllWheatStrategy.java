package io.github.pineconelp.wheateconomy.bank.withdraw;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class WithdrawAllWheatStrategy implements WithdrawStrategy {

  @Override
  public boolean isValidAmount() {
    return true;
  }

  @Override
  public boolean canWithdraw(int balance, int availableWheatSpace, int availableHayBaleSpace) {
    return balance > 0 && availableWheatSpace > 0;
  }

  @Override
  public void sendCannotWithdrawMessage(Player player, int balance, int availableWheatSpace, int availableHayBaleSpace) {
    if (balance == 0) {
      player.sendMessage(Component.text("You have no wheat in your account.", NamedTextColor.RED));
    } else {
      player.sendMessage(
          Component.text("You don't have enough space in your inventory to withdraw wheat.", NamedTextColor.RED));
    }
  }

  @Override
  public int calculateWheatToDeduct(int balance, int availableWheatSpace, int availableHayBaleSpace) {
    return Math.min(balance, availableWheatSpace);
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
