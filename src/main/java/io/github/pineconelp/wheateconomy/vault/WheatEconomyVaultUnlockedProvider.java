package io.github.pineconelp.wheateconomy.vault;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

import io.github.pineconelp.wheateconomy.bank.BankRepository;
import io.github.pineconelp.wheateconomy.bank.LedgerEntryType;

import net.milkbowl.vault2.economy.AccountPermission;
import net.milkbowl.vault2.economy.Economy;
import net.milkbowl.vault2.economy.EconomyResponse;
import net.milkbowl.vault2.economy.EconomyResponse.ResponseType;
import net.milkbowl.vault2.economy.MultiEconomyResponse;

public class WheatEconomyVaultUnlockedProvider implements Economy {
  private static final String CURRENCY = "Wheat";

  private final Plugin plugin;
  private final BankRepository bankRepository;

  public WheatEconomyVaultUnlockedProvider(Plugin plugin, BankRepository bankRepository) {
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
  public boolean hasSharedAccountSupport() {
    return false;
  }

  @Override
  public boolean hasMultiCurrencySupport() {
    return false;
  }

  @Override
  public int fractionalDigits(String pluginName) {
    return 0;
  }

  @Override
  public String format(BigDecimal amount) {
    return toWholeWheat(amount) + " " + CURRENCY;
  }

  @Override
  public String format(String pluginName, BigDecimal amount) {
    return format(amount);
  }

  @Override
  public String format(BigDecimal amount, String currency) {
    return format(amount);
  }

  @Override
  public String format(String pluginName, BigDecimal amount, String currency) {
    return format(amount);
  }

  @Override
  public boolean hasCurrency(String currency) {
    return isWheat(currency);
  }

  @Override
  public String getDefaultCurrency(String pluginName) {
    return CURRENCY;
  }

  @Override
  public String defaultCurrencyNamePlural(String pluginName) {
    return CURRENCY;
  }

  @Override
  public String defaultCurrencyNameSingular(String pluginName) {
    return CURRENCY;
  }

  @Override
  public Collection<String> currencies() {
    return List.of(CURRENCY);
  }

  @Override
  public boolean createAccount(UUID accountID, String name) {
    return true;
  }

  @Override
  public boolean createAccount(UUID accountID, String name, boolean player) {
    return true;
  }

  @Override
  public boolean createAccount(UUID accountID, String name, String worldName) {
    return true;
  }

  @Override
  public boolean createAccount(UUID accountID, String name, String worldName, boolean player) {
    return true;
  }

  @Override
  public Map<UUID, String> getUUIDNameMap() {
    return Map.of();
  }

  @Override
  public Optional<String> getAccountName(UUID accountID) {
    return Optional.empty();
  }

  @Override
  public boolean hasAccount(UUID accountID) {
    return true;
  }

  @Override
  public boolean hasAccount(UUID accountID, String worldName) {
    return true;
  }

  @Override
  public boolean renameAccount(UUID accountID, String name) {
    return false;
  }

  @Override
  public boolean renameAccount(String pluginName, UUID accountID, String name) {
    return false;
  }

  @Override
  public boolean deleteAccount(String pluginName, UUID accountID) {
    return false;
  }

  @Override
  public boolean accountSupportsCurrency(String pluginName, UUID accountID, String currency) {
    return isWheat(currency);
  }

  @Override
  public boolean accountSupportsCurrency(String pluginName, UUID accountID, String currency, String world) {
    return isWheat(currency);
  }

  @Override
  public BigDecimal getBalance(String pluginName, UUID accountID) {
    return currentBalance(accountID);
  }

  @Override
  public BigDecimal getBalance(String pluginName, UUID accountID, String world) {
    return currentBalance(accountID);
  }

  @Override
  public BigDecimal getBalance(String pluginName, UUID accountID, String world, String currency) {
    if (!isWheat(currency)) {
      return BigDecimal.ZERO;
    }
    return currentBalance(accountID);
  }

  @Override
  public boolean has(String pluginName, UUID accountID, BigDecimal amount) {
    return currentBalance(accountID).compareTo(BigDecimal.valueOf(toWholeWheat(amount))) >= 0;
  }

  @Override
  public boolean has(String pluginName, UUID accountID, String worldName, BigDecimal amount) {
    return has(pluginName, accountID, amount);
  }

  @Override
  public boolean has(String pluginName, UUID accountID, String worldName, String currency, BigDecimal amount) {
    if (!isWheat(currency)) {
      return false;
    }
    return has(pluginName, accountID, amount);
  }

  @Override
  public EconomyResponse canWithdraw(String pluginName, UUID accountID, BigDecimal amount) {
    int amt = toWholeWheat(amount);
    BigDecimal balance = currentBalance(accountID);

    if (amt <= 0) {
      return failure(amount, balance, "Amount must be a positive whole number of wheat.");
    }

    if (balance.compareTo(BigDecimal.valueOf(amt)) < 0) {
      return failure(amount, balance, "Insufficient wheat.");
    }

    return success(BigDecimal.valueOf(amt), balance);
  }

  @Override
  public EconomyResponse canWithdraw(String pluginName, UUID accountID, String worldName, BigDecimal amount) {
    return canWithdraw(pluginName, accountID, amount);
  }

  @Override
  public EconomyResponse canWithdraw(String pluginName, UUID accountID, String worldName, String currency,
      BigDecimal amount) {
    if (!isWheat(currency)) {
      return failure(amount, currentBalance(accountID), "Unknown currency: " + currency);
    }
    return canWithdraw(pluginName, accountID, amount);
  }

  @Override
  public EconomyResponse withdraw(String pluginName, UUID accountID, BigDecimal amount) {
    int amt = toWholeWheat(amount);

    if (amt <= 0) {
      return failure(amount, currentBalance(accountID), "Amount must be a positive whole number of wheat.");
    }

    try {
      boolean withdrawn = bankRepository.withdrawByPlayerId(accountID, amt, LedgerEntryType.WITHDRAW);
      BigDecimal balance = currentBalance(accountID);

      if (!withdrawn) {
        return failure(amount, balance, "Insufficient wheat.");
      }

      return success(BigDecimal.valueOf(amt), balance);
    } catch (SQLException e) {
      plugin.getLogger().log(Level.SEVERE, "VaultUnlocked withdraw failed for " + accountID, e);
      return failure(amount, currentBalance(accountID), "A database error occurred.");
    }
  }

  @Override
  public EconomyResponse withdraw(String pluginName, UUID accountID, String worldName, BigDecimal amount) {
    return withdraw(pluginName, accountID, amount);
  }

  @Override
  public EconomyResponse withdraw(String pluginName, UUID accountID, String worldName, String currency,
      BigDecimal amount) {
    if (!isWheat(currency)) {
      return failure(amount, currentBalance(accountID), "Unknown currency: " + currency);
    }
    return withdraw(pluginName, accountID, amount);
  }

  @Override
  public EconomyResponse canDeposit(String pluginName, UUID accountID, BigDecimal amount) {
    int amt = toWholeWheat(amount);
    BigDecimal balance = currentBalance(accountID);

    if (amt <= 0) {
      return failure(amount, balance, "Amount must be a positive whole number of wheat.");
    }

    return success(BigDecimal.valueOf(amt), balance);
  }

  @Override
  public EconomyResponse canDeposit(String pluginName, UUID accountID, String worldName, BigDecimal amount) {
    return canDeposit(pluginName, accountID, amount);
  }

  @Override
  public EconomyResponse canDeposit(String pluginName, UUID accountID, String worldName, String currency,
      BigDecimal amount) {
    if (!isWheat(currency)) {
      return failure(amount, currentBalance(accountID), "Unknown currency: " + currency);
    }
    return canDeposit(pluginName, accountID, amount);
  }

  @Override
  public EconomyResponse deposit(String pluginName, UUID accountID, BigDecimal amount) {
    int amt = toWholeWheat(amount);

    if (amt <= 0) {
      return failure(amount, currentBalance(accountID), "Amount must be a positive whole number of wheat.");
    }

    try {
      bankRepository.depositByPlayerId(accountID, amt, LedgerEntryType.DEPOSIT);
      return success(BigDecimal.valueOf(amt), currentBalance(accountID));
    } catch (SQLException e) {
      plugin.getLogger().log(Level.SEVERE, "VaultUnlocked deposit failed for " + accountID, e);
      return failure(amount, currentBalance(accountID), "A database error occurred.");
    }
  }

  @Override
  public EconomyResponse deposit(String pluginName, UUID accountID, String worldName, BigDecimal amount) {
    return deposit(pluginName, accountID, amount);
  }

  @Override
  public EconomyResponse deposit(String pluginName, UUID accountID, String worldName, String currency,
      BigDecimal amount) {
    if (!isWheat(currency)) {
      return failure(amount, currentBalance(accountID), "Unknown currency: " + currency);
    }
    return deposit(pluginName, accountID, amount);
  }

  @Override
  public EconomyResponse set(String pluginName, UUID accountID, BigDecimal amount) {
    int amt = toWholeWheat(amount);

    try {
      bankRepository.setBalanceByPlayerId(accountID, amt);
      return success(BigDecimal.valueOf(amt), currentBalance(accountID));
    } catch (SQLException e) {
      plugin.getLogger().log(Level.SEVERE, "VaultUnlocked set failed for " + accountID, e);
      return failure(amount, currentBalance(accountID), "A database error occurred.");
    }
  }

  @Override
  public EconomyResponse set(String pluginName, UUID accountID, String worldName, BigDecimal amount) {
    return set(pluginName, accountID, amount);
  }

  @Override
  public EconomyResponse set(String pluginName, UUID accountID, String worldName, String currency, BigDecimal amount) {
    if (!isWheat(currency)) {
      return failure(amount, currentBalance(accountID), "Unknown currency: " + currency);
    }
    return set(pluginName, accountID, amount);
  }

  @Override
  public MultiEconomyResponse transfer(String pluginName, UUID from, UUID to, BigDecimal amount) {
    int amt = toWholeWheat(amount);

    if (amt <= 0) {
      return new MultiEconomyResponse(amount, ResponseType.FAILURE, "Amount must be a positive whole number of wheat.");
    }

    try {
      boolean transferred = bankRepository.transferByPlayerId(from, to, amt);

      if (!transferred) {
        return new MultiEconomyResponse(amount, ResponseType.FAILURE, "Insufficient wheat.");
      }

      MultiEconomyResponse response = new MultiEconomyResponse(BigDecimal.valueOf(amt), ResponseType.SUCCESS, "");
      response.addBalance(from, currentBalance(from));
      response.addBalance(to, currentBalance(to));
      return response;
    } catch (SQLException e) {
      plugin.getLogger().log(Level.SEVERE, "VaultUnlocked transfer failed from " + from + " to " + to, e);
      return new MultiEconomyResponse(amount, ResponseType.FAILURE, "A database error occurred.");
    }
  }

  @Override
  public MultiEconomyResponse transfer(String pluginName, UUID from, UUID to, String worldName, BigDecimal amount) {
    return transfer(pluginName, from, to, amount);
  }

  @Override
  public MultiEconomyResponse transfer(String pluginName, UUID from, UUID to, String worldName, String currency,
      BigDecimal amount) {
    if (!isWheat(currency)) {
      return new MultiEconomyResponse(amount, ResponseType.FAILURE, "Unknown currency: " + currency);
    }
    return transfer(pluginName, from, to, amount);
  }

  @Override
  public boolean createSharedAccount(String pluginName, UUID accountID, String name, UUID owner) {
    return false;
  }

  @Override
  public boolean isAccountOwner(String pluginName, UUID accountID, UUID uuid) {
    return false;
  }

  @Override
  public boolean setOwner(String pluginName, UUID accountID, UUID uuid) {
    return false;
  }

  @Override
  public boolean isAccountMember(String pluginName, UUID accountID, UUID uuid) {
    return false;
  }

  @Override
  public boolean addAccountMember(String pluginName, UUID accountID, UUID uuid) {
    return false;
  }

  @Override
  public boolean addAccountMember(String pluginName, UUID accountID, UUID uuid,
      AccountPermission... initialPermissions) {
    return false;
  }

  @Override
  public boolean removeAccountMember(String pluginName, UUID accountID, UUID uuid) {
    return false;
  }

  @Override
  public boolean hasAccountPermission(String pluginName, UUID accountID, UUID uuid, AccountPermission permission) {
    return false;
  }

  @Override
  public boolean updateAccountPermission(String pluginName, UUID accountID, UUID uuid, AccountPermission permission,
      boolean value) {
    return false;
  }

  private boolean isWheat(String currency) {
    return CURRENCY.equalsIgnoreCase(currency);
  }

  private BigDecimal currentBalance(UUID accountID) {
    try {
      return BigDecimal.valueOf(bankRepository.getBalanceByPlayerId(accountID));
    } catch (SQLException e) {
      plugin.getLogger().log(Level.SEVERE, "Failed to read wheat balance for " + accountID, e);
      return BigDecimal.ZERO;
    }
  }

  private EconomyResponse failure(BigDecimal amount, BigDecimal balance, String message) {
    return new EconomyResponse(amount, balance, ResponseType.FAILURE, message);
  }

  private EconomyResponse success(BigDecimal amount, BigDecimal balance) {
    return new EconomyResponse(amount, balance, ResponseType.SUCCESS, "");
  }

  private static int toWholeWheat(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      return 0;
    }
    return amount.setScale(0, RoundingMode.FLOOR).intValue();
  }
}
