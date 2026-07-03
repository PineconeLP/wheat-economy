package io.github.pineconelp.wheateconomy.vault;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import io.github.pineconelp.wheateconomy.bank.BankRepository;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class WheatEconomyProvider implements Economy {
  private static final String CURRENCY = "Wheat";

  private final Plugin plugin;
  private final BankRepository bankRepository;

  public WheatEconomyProvider(Plugin plugin, BankRepository bankRepository) {
    this.plugin = plugin;
    this.bankRepository = bankRepository;
  }

  @Override
  public boolean isEnabled() {
    return plugin.isEnabled();
  }

  @Override
  public String getName() {
    return "WheatEconomy";
  }

  @Override
  public boolean hasBankSupport() {
    return false;
  }

  @Override
  public int fractionalDigits() {
    return 0;
  }

  @Override
  public String format(double amount) {
    return toWholeWheat(amount) + " " + CURRENCY;
  }

  @Override
  public String currencyNamePlural() {
    return CURRENCY;
  }

  @Override
  public String currencyNameSingular() {
    return CURRENCY;
  }

  private static int toWholeWheat(double amount) {
    if (amount <= 0) {
      return 0;
    }
    return (int) Math.floor(amount);
  }

  private int readBalance(UUID playerId) {
    try {
      return bankRepository.getBalanceByPlayerId(playerId);
    } catch (SQLException e) {
      plugin.getLogger().log(Level.SEVERE, "Failed to read wheat balance for " + playerId, e);
      return 0;
    }
  }

  private EconomyResponse failure(double amount, double balance, String message) {
    return new EconomyResponse(amount, balance, ResponseType.FAILURE, message);
  }

  private EconomyResponse success(double amount, double balance) {
    return new EconomyResponse(amount, balance, ResponseType.SUCCESS, null);
  }

  private EconomyResponse notImplemented() {
    return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "WheatEconomy does not support this operation.");
  }

  @Override
  public boolean hasAccount(OfflinePlayer player) {
    return true;
  }

  @Override
  public boolean hasAccount(OfflinePlayer player, String worldName) {
    return true;
  }

  @Override
  public boolean hasAccount(String playerName) {
    return true;
  }

  @Override
  public boolean hasAccount(String playerName, String worldName) {
    return true;
  }

  @Override
  public boolean createPlayerAccount(OfflinePlayer player) {
    return true;
  }

  @Override
  public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
    return true;
  }

  @Override
  public boolean createPlayerAccount(String playerName) {
    return true;
  }

  @Override
  public boolean createPlayerAccount(String playerName, String worldName) {
    return true;
  }

  @Override
  public double getBalance(OfflinePlayer player) {
    return readBalance(player.getUniqueId());
  }

  @Override
  public double getBalance(OfflinePlayer player, String world) {
    return getBalance(player);
  }

  @Override
  public double getBalance(String playerName) {
    return getBalance(Bukkit.getOfflinePlayer(playerName));
  }

  @Override
  public double getBalance(String playerName, String world) {
    return getBalance(Bukkit.getOfflinePlayer(playerName));
  }

  @Override
  public boolean has(OfflinePlayer player, double amount) {
    return getBalance(player) >= toWholeWheat(amount);
  }

  @Override
  public boolean has(OfflinePlayer player, String worldName, double amount) {
    return has(player, amount);
  }

  @Override
  public boolean has(String playerName, double amount) {
    return has(Bukkit.getOfflinePlayer(playerName), amount);
  }

  @Override
  public boolean has(String playerName, String worldName, double amount) {
    return has(Bukkit.getOfflinePlayer(playerName), amount);
  }

  @Override
  public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
    UUID playerId = player.getUniqueId();
    int amt = toWholeWheat(amount);

    if (amt <= 0) {
      return failure(amount, readBalance(playerId), "Amount must be a positive whole number of wheat.");
    }

    try {
      boolean withdrawn = bankRepository.withdrawByPlayerId(playerId, amt);
      int balance = readBalance(playerId);

      if (!withdrawn) {
        return failure(amount, balance, "Insufficient wheat.");
      }

      return success(amt, balance);
    } catch (SQLException e) {
      plugin.getLogger().log(Level.SEVERE, "Vault withdraw failed for " + playerId, e);
      return failure(amount, readBalance(playerId), "A database error occurred.");
    }
  }

  @Override
  public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
    return withdrawPlayer(player, amount);
  }

  @Override
  public EconomyResponse withdrawPlayer(String playerName, double amount) {
    return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
  }

  @Override
  public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
    return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
  }

  @Override
  public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
    UUID playerId = player.getUniqueId();
    int amt = toWholeWheat(amount);

    if (amt <= 0) {
      return failure(amount, readBalance(playerId), "Amount must be a positive whole number of wheat.");
    }

    try {
      bankRepository.depositByPlayerId(playerId, amt);
      return success(amt, readBalance(playerId));
    } catch (SQLException e) {
      plugin.getLogger().log(Level.SEVERE, "Vault deposit failed for " + playerId, e);
      return failure(amount, readBalance(playerId), "A database error occurred.");
    }
  }

  @Override
  public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
    return depositPlayer(player, amount);
  }

  @Override
  public EconomyResponse depositPlayer(String playerName, double amount) {
    return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
  }

  @Override
  public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
    return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
  }

  @Override
  public EconomyResponse createBank(String name, OfflinePlayer player) {
    return notImplemented();
  }

  @Override
  public EconomyResponse createBank(String name, String player) {
    return notImplemented();
  }

  @Override
  public EconomyResponse deleteBank(String name) {
    return notImplemented();
  }

  @Override
  public EconomyResponse bankBalance(String name) {
    return notImplemented();
  }

  @Override
  public EconomyResponse bankHas(String name, double amount) {
    return notImplemented();
  }

  @Override
  public EconomyResponse bankWithdraw(String name, double amount) {
    return notImplemented();
  }

  @Override
  public EconomyResponse bankDeposit(String name, double amount) {
    return notImplemented();
  }

  @Override
  public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
    return notImplemented();
  }

  @Override
  public EconomyResponse isBankOwner(String name, String playerName) {
    return notImplemented();
  }

  @Override
  public EconomyResponse isBankMember(String name, OfflinePlayer player) {
    return notImplemented();
  }

  @Override
  public EconomyResponse isBankMember(String name, String playerName) {
    return notImplemented();
  }

  @Override
  public List<String> getBanks() {
    return Collections.emptyList();
  }
}
