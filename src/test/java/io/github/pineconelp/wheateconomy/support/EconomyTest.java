package io.github.pineconelp.wheateconomy.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.sqlite.SQLiteDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.scheduler.BukkitSchedulerMock;

import io.github.pineconelp.wheateconomy.WheatEconomyPlugin;

public abstract class EconomyTest {
  private static final String TEST_JDBC_URL = "jdbc:sqlite:file:wheateconomy-test?mode=memory&cache=shared";

  protected ServerMock server;
  protected WheatEconomyPlugin plugin;
  private Connection keepAlive;

  @BeforeEach
  void startEconomy() throws SQLException {
    keepAlive = DriverManager.getConnection(TEST_JDBC_URL);

    server = MockBukkit.mock();
    plugin = MockBukkit.load(WheatEconomyPlugin.class);
  }

  @AfterEach
  void stopEconomy() throws SQLException {
    MockBukkit.unmock();
    keepAlive.close();
  }

  protected void drainScheduler() {
    BukkitSchedulerMock scheduler = server.getScheduler();
    scheduler.waitAsyncTasksFinished();

    // TODO: https://github.com/MockBukkit/MockBukkit/issues/1586
    for (int attempt = 0; attempt < 10; attempt++) {
      scheduler.waitAsyncTasksFinished();
      scheduler.performTicks(10);

      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  protected DataSource testDataSource() {
    SQLiteDataSource dataSource = new SQLiteDataSource();
    dataSource.setUrl(TEST_JDBC_URL);
    return dataSource;
  }

  protected PlayerMock addPlayer() {
    return server.addPlayer();
  }

  protected PlayerMock addPlayerWithItems(Material type, int amount) {
    PlayerMock player = server.addPlayer();
    player.getInventory().addItem(new ItemStack(type, amount));
    return player;
  }

  protected int countInInventory(PlayerMock player, Material type) {
    int total = 0;

    for (ItemStack item : player.getInventory().getContents()) {
      if (item != null && item.getType() == type) {
        total += item.getAmount();
      }
    }

    return total;
  }

  protected int balanceOf(UUID playerId) throws SQLException {
    try (PreparedStatement stmt = keepAlive.prepareStatement(
        "SELECT balance FROM bank_accounts WHERE player_id = ?")) {
      stmt.setString(1, playerId.toString());

      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          return 0;
        }

        return rs.getInt("balance");
      }
    }
  }

  protected record LedgerEntry(String type, int balanceChange, int balanceAfter, UUID targetPlayerId) {
  }

  protected List<LedgerEntry> ledgerOf(UUID playerId) throws SQLException {
    List<LedgerEntry> entries = new ArrayList<>();

    try (PreparedStatement stmt = keepAlive.prepareStatement(
        "SELECT type, balance_change, balance_after, target_player_id FROM bank_ledger " +
            "WHERE player_id = ? ORDER BY created_at, rowid")) {
      stmt.setString(1, playerId.toString());

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          String targetPlayerId = rs.getString("target_player_id");

          entries.add(new LedgerEntry(
              rs.getString("type"),
              rs.getInt("balance_change"),
              rs.getInt("balance_after"),
              targetPlayerId == null ? null : UUID.fromString(targetPlayerId)));
        }
      }
    }

    return entries;
  }

  protected void seedBalance(UUID playerId, int amount) throws SQLException {
    try (PreparedStatement stmt = keepAlive.prepareStatement(
        "INSERT INTO bank_accounts (id, player_id, balance) VALUES (?, ?, ?) " +
            "ON CONFLICT(player_id) DO UPDATE SET balance = ?")) {
      stmt.setString(1, UUID.randomUUID().toString());
      stmt.setString(2, playerId.toString());
      stmt.setLong(3, amount);
      stmt.setLong(4, amount);
      stmt.executeUpdate();
    }
  }
}
