package io.github.pineconelp.wheateconomy.bank;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class BankAccount {
  private UUID playerId;
  private int balance;

  public BankAccount(UUID playerId, int balance) {
    super();

    this.playerId = playerId;
    this.balance = balance;
  }

  public UUID getPlayerId() {
    return playerId;
  }

  public int getBalance() {
    return balance;
  }

  public String getPlayerName() {
    OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);

    if (player == null) {
      return "Unknown";
    }

    return player.getName();
  }
}
