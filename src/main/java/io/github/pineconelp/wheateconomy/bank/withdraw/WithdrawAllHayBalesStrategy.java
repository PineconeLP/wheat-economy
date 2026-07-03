package io.github.pineconelp.wheateconomy.bank.withdraw;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class WithdrawAllHayBalesStrategy implements WithdrawStrategy {

  @Override
  public boolean isValidAmount() {
    return true;
  }

  @Override
  public boolean canWithdraw(int balance, int availableWheatSpace, int availableHayBaleSpace) {
    return balance >= 9 && availableHayBaleSpace > 0;
  }

  @Override
  public void sendCannotWithdrawMessage(Player player, int balance, int availableWheatSpace, int availableHayBaleSpace) {
    if (balance < 9) {
      player.sendMessage(Component.text(
          "You don't have enough wheat in your account to withdraw a hay bale. (Requires 9 wheat)",
          NamedTextColor.RED));
    } else {
      player.sendMessage(
          Component.text("You don't have enough space in your inventory to withdraw hay bales.", NamedTextColor.RED));
    }
  }

  @Override
  public int calculateWheatToDeduct(int balance, int availableWheatSpace, int availableHayBaleSpace) {
    return Math.min(balance / 9, availableHayBaleSpace) * 9;
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
