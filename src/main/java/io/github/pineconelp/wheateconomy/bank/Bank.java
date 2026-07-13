package io.github.pineconelp.wheateconomy.bank;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.List;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import io.github.pineconelp.wheateconomy.bank.deposit.DepositStrategy;
import io.github.pineconelp.wheateconomy.bank.withdraw.WithdrawStrategy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;


public class Bank {
  private static final int ENTRIES_PER_PAGE = 5;
  private static final String SEPARATOR = "=".repeat(51);
  private static final DateTimeFormatter DATE_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

  private BankRepository bankRepository;
  private Plugin plugin;
  private Set<UUID> transactingPlayerIds;

  public Bank(BankRepository bankRepository, Plugin plugin, Set<UUID> transactingPlayerIds) {
    super();

    this.bankRepository = bankRepository;
    this.plugin = plugin;
    this.transactingPlayerIds = transactingPlayerIds;
  }

  public void showSummary(Player player) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      int balance = getBalanceDisplay(player.getUniqueId());

      Bukkit.getScheduler().runTask(plugin, () -> {
        player.sendMessage(
            Component.text("=== Bank ===", NamedTextColor.GOLD));
        player.sendMessage(
            Component.text("Your Balance: " + balance + " Wheat", NamedTextColor.GRAY));
        player.sendMessage("\n");
        player.sendMessage(
            Component.text("Click below to manage your wheat.", NamedTextColor.GRAY));

        Component depositButton = Component.text("[Deposit]", NamedTextColor.GOLD)
            .clickEvent(ClickEvent.suggestCommand("/bank deposit "))
            .hoverEvent(HoverEvent.showText(
                Component.text("Click to deposit wheat")));
        Component withdrawButton = Component.text("[Withdraw]", NamedTextColor.GOLD)
            .clickEvent(ClickEvent.suggestCommand("/bank withdraw "))
            .hoverEvent(HoverEvent.showText(
                Component.text("Click to withdraw wheat")));
        Component sendButton = Component.text("[Send]", NamedTextColor.GOLD)
            .clickEvent(ClickEvent.suggestCommand("/bank send "))
            .hoverEvent(HoverEvent.showText(
                Component.text("Click to send wheat to another player")));

        player.sendMessage(Component.join(JoinConfiguration.separator(Component.text(" ")), depositButton,
            withdrawButton, sendButton));
      });
    });
  }

  public void showBalance(Player player) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      int balance = getBalanceDisplay(player.getUniqueId());

      Bukkit.getScheduler().runTask(plugin, () -> {
        player.sendMessage(
            Component.text("=== Bank ===", NamedTextColor.GOLD));
        player.sendMessage(
            Component.text("Your Balance: " + balance + " Wheat", NamedTextColor.GRAY));
      });
    });
  }

  private int getBalanceDisplay(UUID playerId) {
    try {
      return this.bankRepository.getBalanceByPlayerId(playerId);
    } catch (SQLException e) {
      plugin.getLogger().log(Level.SEVERE, "Failed to read bank balance for " + playerId, e);
      return -1;
    }
  }

  private void logTransaction(LedgerEntryType type, UUID playerId, String playerName, int amount) {
    plugin.getLogger().info(
        "BANK_" + type.name() + " player=" + playerName + " uuid=" + playerId + " amount=" + amount);
  }

  private void logTransfer(UUID senderId, String senderName, UUID targetId, String targetName, int amount) {
    plugin.getLogger().info("BANK_SEND"
        + " sender=" + senderName + " senderUuid=" + senderId
        + " target=" + targetName + " targetUuid=" + targetId
        + " amount=" + amount);
  }

  public void deposit(Player player, DepositStrategy strategy) {
    UUID playerId = player.getUniqueId();

    if (transactingPlayerIds.contains(playerId)) {
      player.sendMessage(
          Component.text("You have a bank transaction in progress. Please try again.", NamedTextColor.RED));
      return;
    }

    transactingPlayerIds.add(playerId);

    if (!strategy.isValidAmount()) {
      player.sendMessage(Component.text("Please enter a valid amount.", NamedTextColor.RED));
      transactingPlayerIds.remove(playerId);
      return;
    }

    int playerHayBaleAmount = countItemInPlayerInventory(player, Material.HAY_BLOCK);
    int playerWheatAmount = countItemInPlayerInventory(player, Material.WHEAT);

    if (!strategy.isSufficientAmount(playerHayBaleAmount, playerWheatAmount)) {
      strategy.sendInsufficientAmountMessage(player, playerHayBaleAmount, playerWheatAmount);
      transactingPlayerIds.remove(playerId);
      return;
    }

    int amountToDeposit = strategy.calculateAmountToDeposit(playerHayBaleAmount, playerWheatAmount);

    if (amountToDeposit == 0) {
      player.sendMessage(
          Component.text("You have nothing to deposit.", NamedTextColor.RED));
      transactingPlayerIds.remove(playerId);
      return;
    }

    takeItemFromPlayerInventory(player, Material.HAY_BLOCK, strategy.calculateHayBaleAmountToTake(playerHayBaleAmount));
    takeItemFromPlayerInventory(player, Material.WHEAT, strategy.calculateWheatAmountToTake(playerWheatAmount));

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      try {
        bankRepository.depositByPlayerId(player.getUniqueId(), amountToDeposit, LedgerEntryType.DEPOSIT);
        logTransaction(LedgerEntryType.DEPOSIT, playerId, player.getName(), amountToDeposit);

        Bukkit.getScheduler().runTask(plugin, () -> {
          strategy.sendDepositSuccessMessage(player, playerHayBaleAmount, playerWheatAmount);
          transactingPlayerIds.remove(playerId);
        });
      } catch (SQLException e) {
        plugin.getLogger().log(Level.SEVERE, "Failed to deposit wheat for " + playerId, e);
        Bukkit.getScheduler().runTask(plugin, () -> {
          player.sendMessage(Component.text("Failed to deposit. Please try again later.", NamedTextColor.RED));
        });
        transactingPlayerIds.remove(playerId);
      }
    });
  }

  private int countItemInPlayerInventory(Player player, Material itemType) {
    int total = 0;

    PlayerInventory inventory = player.getInventory();

    for (int i = 0; i < inventory.getSize(); i++) {
      ItemStack item = inventory.getItem(i);

      if (item != null && item.getType() == itemType) {
        total += item.getAmount();
      }
    }

    return total;
  }

  private void takeItemFromPlayerInventory(Player player, Material itemType, int amountToTake) {
    int remainingAmountToRemove = amountToTake;

    PlayerInventory inventory = player.getInventory();

    for (int i = 0; i < inventory.getSize(); i++) {
      ItemStack item = inventory.getItem(i);

      if (item != null && item.getType() == itemType) {
        int amount = item.getAmount();

        boolean shouldRemoveEntireStack = amount <= remainingAmountToRemove;

        if (shouldRemoveEntireStack) {
          remainingAmountToRemove -= amount;
          inventory.clear(i);
          continue;
        }

        item.setAmount(amount - remainingAmountToRemove);
        remainingAmountToRemove = 0;
        break;
      }
    }
  }

  private int countAvailableWheatSpace(Player player) {
    int space = 0;

    for (ItemStack content : player.getInventory().getStorageContents()) {
      if (content == null) {
        space += 64;
      } else if (content.getType() == Material.WHEAT) {
        space += (64 - content.getAmount());
      }
    }

    return space;
  }

  private int countAvailableHayBaleSpace(Player player) {
    int space = 0;

    for (ItemStack content : player.getInventory().getStorageContents()) {
      if (content == null) {
        space += 64;
      } else if (content.getType() == Material.HAY_BLOCK) {
        space += (64 - content.getAmount());
      }
    }

    return space;
  }

  public void withdraw(Player player, WithdrawStrategy strategy) {
    UUID playerId = player.getUniqueId();

    if (transactingPlayerIds.contains(playerId)) {
      player.sendMessage(
          Component.text("You have a bank transaction in progress. Please try again.", NamedTextColor.RED));
      return;
    }

    transactingPlayerIds.add(playerId);

    if (!strategy.isValidAmount()) {
      transactingPlayerIds.remove(playerId);
      player.sendMessage(Component.text("Please enter a valid amount.", NamedTextColor.RED));
      return;
    }

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      try {
        int balance = bankRepository.getBalanceByPlayerId(player.getUniqueId());

        Bukkit.getScheduler().runTask(plugin, () -> {
          int availableWheatSpace = countAvailableWheatSpace(player);
          int availableHayBaleSpace = countAvailableHayBaleSpace(player);

          if (!strategy.canWithdraw(balance, availableWheatSpace, availableHayBaleSpace)) {
            strategy.sendCannotWithdrawMessage(player, balance, availableWheatSpace, availableHayBaleSpace);
            transactingPlayerIds.remove(playerId);
            return;
          }

          int wheatToDeduct = strategy.calculateWheatToDeduct(balance, availableWheatSpace, availableHayBaleSpace);

          if (wheatToDeduct == 0) {
            player.sendMessage(Component.text("You have nothing to withdraw.", NamedTextColor.RED));
            transactingPlayerIds.remove(playerId);
            return;
          }

          Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
              boolean withdrawn = bankRepository.withdrawByPlayerId(player.getUniqueId(), wheatToDeduct,
                  LedgerEntryType.WITHDRAW);

              if (!withdrawn) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                  player.sendMessage(Component.text("You have insufficient wheat to withdraw.", NamedTextColor.RED));
                  transactingPlayerIds.remove(playerId);
                });
                return;
              }

              logTransaction(LedgerEntryType.WITHDRAW, playerId, player.getName(), wheatToDeduct);

              Bukkit.getScheduler().runTask(plugin, () -> {
                strategy.giveItemsToPlayer(player, wheatToDeduct);
                strategy.sendSuccessMessage(player, wheatToDeduct);
                transactingPlayerIds.remove(playerId);
              });
            } catch (SQLException e) {
              plugin.getLogger().log(Level.SEVERE, "Failed to withdraw wheat for " + playerId, e);
              Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(Component.text("Failed to withdraw. Please try again.", NamedTextColor.RED));
              });
              transactingPlayerIds.remove(playerId);
            }
          });
        });
      } catch (SQLException e) {
        plugin.getLogger().log(Level.SEVERE, "Failed to read bank balance for " + player.getUniqueId(), e);
        Bukkit.getScheduler().runTask(plugin, () -> {
          player.sendMessage(Component.text("Failed to withdraw. Please try again.", NamedTextColor.RED));
        });
        transactingPlayerIds.remove(playerId);
      }
    });
  }

  public void send(Player sender, String targetPlayerName, int amountToSend) {
    Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

    if (targetPlayer == null || !targetPlayer.isOnline()) {
      sender.sendMessage(
          Component.text(targetPlayerName + " is not online.", NamedTextColor.RED));
      return;
    }

    UUID senderId = sender.getUniqueId();
    UUID targetId = targetPlayer.getUniqueId();

    if (transactingPlayerIds.contains(senderId)) {
      sender.sendMessage(
          Component.text("You have a bank transaction in progress. Please try again.", NamedTextColor.RED));
      return;
    }

    if (transactingPlayerIds.contains(targetId)) {
      sender.sendMessage(
          Component.text(targetPlayerName + " has a bank transaction in progress. Please try again.",
              NamedTextColor.RED));
      return;
    }

    transactingPlayerIds.add(senderId);
    transactingPlayerIds.add(targetId);

    if (amountToSend <= 0) {
      sender.sendMessage(Component.text("Please enter a valid amount.", NamedTextColor.RED));
      transactingPlayerIds.remove(senderId);
      transactingPlayerIds.remove(targetId);
      return;
    }

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      try {
        boolean transferred = bankRepository.transferByPlayerId(senderId, targetId, amountToSend);

        if (!transferred) {
          int senderBalance = bankRepository.getBalanceByPlayerId(senderId);
          Bukkit.getScheduler().runTask(plugin, () -> {
            sender.sendMessage(
                Component.text("Unable to send " + amountToSend + " wheat. You only have " + senderBalance
                    + " wheat in your account.", NamedTextColor.RED));
          });
          transactingPlayerIds.remove(senderId);
          transactingPlayerIds.remove(targetId);
          return;
        }

        logTransfer(senderId, sender.getName(), targetId, targetPlayerName, amountToSend);

        Bukkit.getScheduler().runTask(plugin, () -> {
          sender.sendMessage(
              Component.text("Successfully sent " + amountToSend + " wheat to " + targetPlayerName + "!",
                  NamedTextColor.GREEN));
          targetPlayer.sendMessage(
              Component.text(sender.getName() + " sent you " + amountToSend + " wheat!", NamedTextColor.GREEN));

          transactingPlayerIds.remove(senderId);
          transactingPlayerIds.remove(targetId);
        });
      } catch (SQLException ex) {
        plugin.getLogger().log(Level.SEVERE, "Failed to transfer wheat from " + senderId + " to " + targetId, ex);
        Bukkit.getScheduler().runTask(plugin, () -> {
          sender.sendMessage(Component.text("Failed to transfer wheat. Please try again.", NamedTextColor.RED));
        });
        transactingPlayerIds.remove(senderId);
        transactingPlayerIds.remove(targetId);
      }
    });
  }

  public void getPlayerBalance(CommandSender sender, String playerName) {
    UUID targetPlayerId = Bukkit.getOfflinePlayer(playerName).getUniqueId();

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      int wheatCount = getBalanceDisplay(targetPlayerId);

      Bukkit.getScheduler().runTask(plugin, () -> {
        sender.sendMessage(
            Component.text("Balance: " + wheatCount + " wheat", NamedTextColor.GOLD));
      });
    });
  }

  public void setPlayerBalance(CommandSender sender, String playerName, int amount) {
    UUID targetPlayerId = Bukkit.getOfflinePlayer(playerName).getUniqueId();

    if (transactingPlayerIds.contains(targetPlayerId)) {
      sender.sendMessage(
          Component.text("This player has a bank transaction in progress. Please try again.", NamedTextColor.RED));
      return;
    }

    transactingPlayerIds.add(targetPlayerId);

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      try {
        bankRepository.setBalanceByPlayerId(targetPlayerId, amount);
        logTransaction(LedgerEntryType.ADMIN_SET, targetPlayerId, playerName, amount);
        Bukkit.getScheduler().runTask(plugin, () -> {
          sender.sendMessage(
              Component.text("Done.", NamedTextColor.GREEN));
        });
        transactingPlayerIds.remove(targetPlayerId);
      } catch (SQLException e) {
        plugin.getLogger().log(Level.SEVERE, "Failed to set bank balance for " + targetPlayerId, e);
        Bukkit.getScheduler().runTask(plugin, () -> {
          sender.sendMessage(
              Component.text("Error.", NamedTextColor.RED));
        });
        transactingPlayerIds.remove(targetPlayerId);
      }
    });
  }

  public void addPlayerBalance(CommandSender sender, String playerName, int amount) {
    UUID targetPlayerId = Bukkit.getOfflinePlayer(playerName).getUniqueId();

    if (transactingPlayerIds.contains(targetPlayerId)) {
      sender.sendMessage(
          Component.text("This player has a bank transaction in progress. Please try again.", NamedTextColor.RED));
      return;
    }

    transactingPlayerIds.add(targetPlayerId);

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      try {
        bankRepository.depositByPlayerId(targetPlayerId, amount, LedgerEntryType.ADMIN_ADD);
        logTransaction(LedgerEntryType.ADMIN_ADD, targetPlayerId, playerName, amount);
        Bukkit.getScheduler().runTask(plugin, () -> {
          sender.sendMessage(
              Component.text("Done.", NamedTextColor.GREEN));
        });
        transactingPlayerIds.remove(targetPlayerId);
      } catch (SQLException e) {
        plugin.getLogger().log(Level.SEVERE, "Failed to add bank balance for " + targetPlayerId, e);
        Bukkit.getScheduler().runTask(plugin, () -> {
          sender.sendMessage(
              Component.text("Error.", NamedTextColor.RED));
        });
        transactingPlayerIds.remove(targetPlayerId);
      }
    });
  }

  public void showTransactionHistory(CommandSender sender, String targetName, int page) {

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      try {
        Player targetPlayer = Bukkit.getPlayerExact(targetName);

        if (targetPlayer == null) {
          Bukkit.getScheduler().runTask(plugin, () ->
                  sender.sendMessage(Component.text("Player not found: " + targetName, NamedTextColor.RED)));
          return;
        }

        UUID targetId = targetPlayer.getUniqueId();
        List<LedgerEntry> entries = bankRepository.getLedgerForPlayer(
                targetId, ENTRIES_PER_PAGE, page);

        int totalTransactions = bankRepository.getTransactionCount(targetId);
        int maxPages = (totalTransactions + ENTRIES_PER_PAGE - 1)
                / ENTRIES_PER_PAGE;

        Bukkit.getScheduler().runTask(plugin, () ->
                renderHistoryPage(sender, targetName, entries, page, maxPages, totalTransactions));

      } catch (SQLException e) {
        plugin.getLogger().log(Level.SEVERE, "Failed to load transaction history", e);
        Bukkit.getScheduler().runTask(plugin, () ->
                sender.sendMessage(Component.text("Failed to load history. See console for details.", NamedTextColor.RED)));
      }
    });
  }

  private void renderHistoryPage(CommandSender sender, String targetName, List<LedgerEntry> entries,
                                 int page, int maxPages, int totalTransactions) {
    if (entries.isEmpty()) {
      if (page > maxPages && maxPages > 0) {
        sender.sendMessage(Component.text("Page " + page + " does not exist. Max pages: " + maxPages, NamedTextColor.RED));
      } else {
        sender.sendMessage(Component.text("No transaction history found for " + targetName, NamedTextColor.YELLOW));
      }
      return;
    }

    sendHeader(sender, targetName, page, maxPages, totalTransactions);

    for (LedgerEntry entry : entries) {
      sender.sendMessage(formatTransactionLine(entry));
    }

    sender.sendMessage(Component.text(SEPARATOR, NamedTextColor.GOLD));
    sender.sendMessage(buildNavigation(targetName, page, maxPages));
  }

  private void sendHeader(CommandSender sender, String targetName, int page, int maxPages, int totalTransactions) {
    sender.sendMessage(Component.text(SEPARATOR, NamedTextColor.GOLD));
    sender.sendMessage(Component.text("Transaction History: " + targetName, NamedTextColor.GOLD)
            .decorate(TextDecoration.BOLD));
    sender.sendMessage(Component.text(
            "Page " + page + " of " + maxPages + " (Total: " + totalTransactions + " transactions)",
            NamedTextColor.GRAY));
    sender.sendMessage(Component.text(SEPARATOR, NamedTextColor.GOLD));
  }

  private Component formatTransactionLine(LedgerEntry entry) {
    String date = DATE_FORMAT.format(Instant.ofEpochMilli(entry.getCreatedAt()));
    String type = prettifyType(entry.getType());
    String change = String.format("%+d", entry.getBalanceChange());
    String balance = String.valueOf(entry.getBalanceAfter());
    NamedTextColor changeColor = entry.getBalanceChange() >= 0 ? NamedTextColor.GREEN : NamedTextColor.RED;

    return Component.text()
            .append(Component.text("[" + date + "] ", NamedTextColor.GRAY))
            .append(Component.text(type, getTypeColor(entry.getType())))
            .append(Component.text(" | ", NamedTextColor.GRAY))
            .append(Component.text(change, changeColor))
            .append(Component.text(" | Balance: ", NamedTextColor.GRAY))
            .append(Component.text(balance, NamedTextColor.YELLOW))
            .append(resolveTargetInfo(entry))
            .build();
  }

  private Component resolveTargetInfo(LedgerEntry entry) {
    String targetId = entry.getTargetPlayerId();
    if (targetId == null || targetId.isEmpty()) {
      return Component.empty();
    }
    try {
      UUID targetUUID = UUID.fromString(targetId);
      OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetUUID);
      if (offlinePlayer.getName() == null) {
        return Component.empty();
      }
      String arrow = "SEND".equals(entry.getType()) ? " -> " : " <- ";
      return Component.text(arrow + offlinePlayer.getName(), NamedTextColor.DARK_GRAY);
    } catch (IllegalArgumentException e) {
      return Component.empty();
    }
  }

  private Component buildNavigation(String targetName, int page, int maxPages) {
    TextComponent.Builder nav = Component.text();
    if (page > 1) {
      nav.append(pageLink("<< Previous", targetName, page - 1))
              .append(Component.text("  |  ", NamedTextColor.GRAY));
    }
    nav.append(Component.text("Page " + page + "/" + maxPages, NamedTextColor.YELLOW));
    if (page < maxPages) {
      nav.append(Component.text("  |  ", NamedTextColor.GRAY))
              .append(pageLink("Next >>", targetName, page + 1));
    }
    return nav.build();
  }

  private Component pageLink(String label, String targetName, int targetPage) {
    return Component.text(label, NamedTextColor.AQUA)
            .clickEvent(ClickEvent.runCommand("/bank history " + targetName + " " + targetPage))
            .hoverEvent(HoverEvent.showText(Component.text("Go to page " + targetPage)));
  }

  private NamedTextColor getTypeColor(String type) {
    return switch(type) {
      case "DEPOSIT" -> NamedTextColor.GREEN;
      case "WITHDRAW" -> NamedTextColor.RED;
      case "SEND" -> NamedTextColor.BLUE;
      case "RECEIVE" -> NamedTextColor.LIGHT_PURPLE;
      case "ADMIN_SET" -> NamedTextColor.GOLD;
      default -> NamedTextColor.WHITE;
    };
  }

  private String prettifyType(String type) {
    return switch (type) {
      case "DEPOSIT" -> "Deposit";
      case "WITHDRAW" -> "Withdraw";
      case "SEND" -> "Send";
      case "RECEIVE" -> "Receive";
      case "ADMIN_SET" -> "Admin Set";
      case "ADMIN_ADD" -> "Admin Add";
      default -> type;
    };
  }
}
