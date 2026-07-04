package io.github.pineconelp.wheateconomy.bank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

public class BankRepository {
  private final DataSource dataSource;

  public BankRepository(DataSource dataSource) {
    super();
    this.dataSource = dataSource;
  }

  public void initialize() throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement(
          "CREATE TABLE IF NOT EXISTS bank_accounts (" +
              "id TEXT PRIMARY KEY," +
              "player_id TEXT NOT NULL UNIQUE," +
              "balance INTEGER NOT NULL DEFAULT 0" +
              ");")) {
        stmt.executeUpdate();
      }
    }
  }

  public int getBalanceByPlayerId(UUID playerId) throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement(
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
  }

  public void depositByPlayerId(UUID playerId, int amountToDeposit) throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement(
          "INSERT INTO bank_accounts (id, player_id, balance) VALUES (?, ?, ?) " +
              "ON CONFLICT(player_id) DO UPDATE SET balance = balance + ?")) {
        stmt.setString(1, UUID.randomUUID().toString());
        stmt.setString(2, playerId.toString());
        stmt.setLong(3, amountToDeposit);
        stmt.setLong(4, amountToDeposit);
        stmt.executeUpdate();
      }
    }
  }

  public boolean withdrawByPlayerId(UUID playerId, int amountToWithdraw) throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement(
          "UPDATE bank_accounts SET balance = balance - ? WHERE player_id = ? AND balance >= ?")) {
        stmt.setLong(1, amountToWithdraw);
        stmt.setString(2, playerId.toString());
        stmt.setLong(3, amountToWithdraw);
        return stmt.executeUpdate() == 1;
      }
    }
  }

  public boolean transferByPlayerId(UUID senderId, UUID targetId, int amount) throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      conn.setAutoCommit(false);

      try {
        int debited;

        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE bank_accounts SET balance = balance - ? WHERE player_id = ? AND balance >= ?")) {
          stmt.setLong(1, amount);
          stmt.setString(2, senderId.toString());
          stmt.setLong(3, amount);
          debited = stmt.executeUpdate();
        }

        if (debited != 1) {
          conn.rollback();
          return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO bank_accounts (id, player_id, balance) VALUES (?, ?, ?) " +
                "ON CONFLICT(player_id) DO UPDATE SET balance = balance + ?")) {
          stmt.setString(1, UUID.randomUUID().toString());
          stmt.setString(2, targetId.toString());
          stmt.setLong(3, amount);
          stmt.setLong(4, amount);
          stmt.executeUpdate();
        }

        conn.commit();
        return true;
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      }
    }
  }

  public void setBalanceByPlayerId(UUID playerId, int amount) throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      try (PreparedStatement stmt = conn.prepareStatement(
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
}
