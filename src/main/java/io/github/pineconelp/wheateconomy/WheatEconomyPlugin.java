package io.github.pineconelp.wheateconomy;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.pineconelp.wheateconomy.api.WheatEconomyApi;
import io.github.pineconelp.wheateconomy.api.WheatEconomyApiProvider;
import io.github.pineconelp.wheateconomy.bank.BankRepository;
import io.github.pineconelp.wheateconomy.bank.Bank;
import io.github.pineconelp.wheateconomy.commands.BankCommand;
import io.github.pineconelp.wheateconomy.vault.WheatEconomyVaultProvider;
import io.github.pineconelp.wheateconomy.vault.WheatEconomyVaultUnlockedProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.milkbowl.vault.economy.Economy;

public class WheatEconomyPlugin extends JavaPlugin {
  private HikariDataSource dataSource;

  @Override
  public void onEnable() {
    if (!getDataFolder().exists()) {
      getDataFolder().mkdirs();
    }

    saveDefaultConfig();

    try {
      this.dataSource = createDataSource();

      BankRepository bankRepository = new BankRepository(dataSource);
      bankRepository.initialize();

      Bank bank = new Bank(bankRepository, this, ConcurrentHashMap.newKeySet());

      getServer().getServicesManager().register(
          WheatEconomyApi.class, new WheatEconomyApiProvider(bankRepository), this, ServicePriority.Normal);
      getLogger().info("Registered WheatEconomy API service.");

      if (getServer().getPluginManager().getPlugin("Vault") != null) {
        WheatEconomyVaultProvider economyProvider = new WheatEconomyVaultProvider(this, bankRepository);

        getServer().getServicesManager().register(Economy.class, economyProvider, this, ServicePriority.Normal);
        getLogger().info("Registered Vault economy provider: WheatEconomy");
      } else {
        getLogger().warning("Vault not found; skipping economy provider registration. Bank commands remain available.");
      }

      boolean vaultLikePluginPresent = getServer().getPluginManager().getPlugin("VaultUnlocked") != null
          || getServer().getPluginManager().getPlugin("Vault") != null;

      if (vaultLikePluginPresent) {
        try {
          WheatEconomyVaultUnlockedProvider unlockedProvider = new WheatEconomyVaultUnlockedProvider(this, bankRepository);

          getServer().getServicesManager().register(net.milkbowl.vault2.economy.Economy.class, unlockedProvider, this, ServicePriority.Normal);
          getLogger().info("Registered VaultUnlocked economy provider: WheatEconomy");
        } catch (Throwable t) {
          getLogger().warning("VaultUnlocked (Vault2) API not available; skipping VaultUnlocked economy provider registration. Bank commands remain available.");
        }
      } else {
        getLogger().warning("VaultUnlocked not found; skipping VaultUnlocked economy provider registration. Bank commands remain available.");
      }

      this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
        Commands commands = event.registrar();

        commands.register(new BankCommand(bank).create(), "Wheat economy bank commands");
      });
    } catch (SQLException e) {
      getLogger().log(Level.SEVERE, "Failed to initialize the wheat economy database. Disabling plugin.", e);
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
  }

  @Override
  public void onDisable() {
    if (dataSource != null && !dataSource.isClosed()) {
      dataSource.close();
    }
  }

  private HikariDataSource createDataSource() {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setPoolName("WheatEconomy");
    hikariConfig.setDriverClassName("org.sqlite.JDBC");
    hikariConfig.setJdbcUrl(getConfig().getString("database.jdbc-url"));
    hikariConfig.setMaximumPoolSize(1);
    hikariConfig.setMaxLifetime(0);
    hikariConfig.setConnectionInitSql("PRAGMA busy_timeout=5000");

    return new HikariDataSource(hikariConfig);
  }
}
