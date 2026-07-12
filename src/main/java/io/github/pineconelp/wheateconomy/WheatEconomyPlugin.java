package io.github.pineconelp.wheateconomy;

import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Chunk;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.protection.flags.StateFlag;

import io.github.pineconelp.wheateconomy.api.WheatEconomyApi;
import io.github.pineconelp.wheateconomy.api.WheatEconomyApiProvider;
import io.github.pineconelp.wheateconomy.bank.AlwaysNearBankerPolicy;
import io.github.pineconelp.wheateconomy.bank.BankLeaderboard;
import io.github.pineconelp.wheateconomy.bank.BankRepository;
import io.github.pineconelp.wheateconomy.bank.Bank;
import io.github.pineconelp.wheateconomy.bank.BankerLocationPolicy;
import io.github.pineconelp.wheateconomy.commands.BankCommand;
import io.github.pineconelp.wheateconomy.commands.WheatCommand;
import io.github.pineconelp.wheateconomy.listeners.BankTransactionListener;
import io.github.pineconelp.wheateconomy.listeners.WheatGrowthListener;
import io.github.pineconelp.wheateconomy.wheat.WheatGrowthSimulator;
import io.github.pineconelp.wheateconomy.worldguard.WorldGuardBankerRegion;
import io.github.pineconelp.wheateconomy.worldguard.WorldGuardWheatGrowthRegion;
import io.github.pineconelp.wheateconomy.vault.WheatEconomyVaultProvider;
import io.github.pineconelp.wheateconomy.vault.WheatEconomyVaultUnlockedProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.milkbowl.vault.economy.Economy;

public class WheatEconomyPlugin extends JavaPlugin {
  private HikariDataSource dataSource;
  private WorldGuardBankerRegion worldGuardBankerRegion;
  private WorldGuardWheatGrowthRegion worldGuardWheatGrowthRegion;
  private WheatGrowthSimulator wheatGrowthSimulator;

  @Override
  public void onLoad() {
    if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
      worldGuardBankerRegion = new WorldGuardBankerRegion();
      worldGuardBankerRegion.registerFlag();
      getLogger().info("Registered WorldGuard has-banker flag.");

      worldGuardWheatGrowthRegion = new WorldGuardWheatGrowthRegion();
      worldGuardWheatGrowthRegion.registerFlag();
      getLogger().info("Registered WorldGuard wheat-growth flag.");
    } else {
      getLogger().warning("WorldGuard not found; bank access will not be restricted to banker regions.");
    }
  }

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

      Set<UUID> transactingPlayerIds = ConcurrentHashMap.newKeySet();

      Bank bank = new Bank(bankRepository, this, transactingPlayerIds);
      BankLeaderboard bankLeaderboard = new BankLeaderboard(bankRepository, this);

      BankerLocationPolicy locationPolicy = worldGuardBankerRegion != null
          ? worldGuardBankerRegion
          : new AlwaysNearBankerPolicy();

      wheatGrowthSimulator = new WheatGrowthSimulator(this);

      getServer().getPluginManager()
          .registerEvents(new BankTransactionListener(transactingPlayerIds), this);
      getServer().getPluginManager()
          .registerEvents(new WheatGrowthListener(wheatGrowthSimulator, worldGuardWheatGrowthRegion), this);

      getServer().getServicesManager().register(
          WheatEconomyApi.class, new WheatEconomyApiProvider(bank, bankLeaderboard), this,
          ServicePriority.Normal);
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
          WheatEconomyVaultUnlockedProvider unlockedProvider = new WheatEconomyVaultUnlockedProvider(this,
              bankRepository);

          getServer().getServicesManager().register(net.milkbowl.vault2.economy.Economy.class, unlockedProvider, this,
              ServicePriority.Normal);
          getLogger().info("Registered VaultUnlocked economy provider: WheatEconomy");
        } catch (Throwable t) {
          getLogger().warning(
              "VaultUnlocked (Vault2) API not available; skipping VaultUnlocked economy provider registration. Bank commands remain available.");
        }
      } else {
        getLogger().warning(
            "VaultUnlocked not found; skipping VaultUnlocked economy provider registration. Bank commands remain available.");
      }

      this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
        Commands commands = event.registrar();

        commands.register(new BankCommand(bank, bankLeaderboard, locationPolicy).create(),
            "Wheat economy bank commands");
        commands.register(new WheatCommand(wheatGrowthSimulator).create(), "Wheat growth admin commands");
      });
    } catch (SQLException e) {
      getLogger().log(Level.SEVERE, "Failed to initialize the wheat economy database. Disabling plugin.", e);
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
  }

  @Override
  public void onDisable() {
    if (wheatGrowthSimulator != null && getServer().getWorld("world") != null) {
      Chunk[] loadedChunks = getServer().getWorld("world").getLoadedChunks();

      getLogger().log(
          Level.INFO, "WHEAT_GROWTH_SIMULATOR_UNLOADING_CHUNKS: {0}",
          new Object[] { loadedChunks.length });

      for (Chunk chunk : loadedChunks) {
        wheatGrowthSimulator.unload(chunk);
      }
    }

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
