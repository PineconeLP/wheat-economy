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

      try (PreparedStatement stmt = conn.prepareStatement(
          "CREATE TABLE IF NOT EXISTS bank_ledger (" +
              "id TEXT PRIMARY KEY," +
              "player_id TEXT NOT NULL," +
              "type TEXT NOT NULL," +
              "balance_change INTEGER NOT NULL," +
              "balance_after INTEGER NOT NULL," +
              "target_player_id TEXT," +
              "created_at INTEGER NOT NULL" +
              ");")) {
        stmt.executeUpdate();
      }

      try (PreparedStatement stmt = conn.prepareStatement(
          "CREATE INDEX IF NOT EXISTS idx_bank_ledger_player " +
              "ON bank_ledger(player_id, created_at);")) {
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

  public void depositByPlayerId(UUID playerId, int amountToDeposit, LedgerEntryType type) throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      conn.setAutoCommit(false);

      try {
        int balanceAfter = credit(conn, playerId, amountToDeposit);
        recordEntry(conn, playerId, type, amountToDeposit, balanceAfter, null);
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      }
    }
  }

  public boolean withdrawByPlayerId(UUID playerId, int amountToWithdraw, LedgerEntryType type) throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      conn.setAutoCommit(false);

      try {
        Integer balanceAfter = debit(conn, playerId, amountToWithdraw);

        if (balanceAfter == null) {
          conn.rollback();
          return false;
        }

        recordEntry(conn, playerId, type, -amountToWithdraw, balanceAfter, null);
        conn.commit();
        return true;
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      }
    }
  }

  public boolean transferByPlayerId(UUID senderId, UUID targetId, int amount) throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      conn.setAutoCommit(false);

      try {
        Integer senderBalanceAfter = debit(conn, senderId, amount);

        if (senderBalanceAfter == null) {
          conn.rollback();
          return false;
        }

        recordEntry(conn, senderId, LedgerEntryType.SEND, -amount, senderBalanceAfter, targetId);

        int targetBalanceAfter = credit(conn, targetId, amount);
        recordEntry(conn, targetId, LedgerEntryType.RECEIVE, amount, targetBalanceAfter, senderId);

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
      conn.setAutoCommit(false);

      try {
        int previousBalance = readBalance(conn, playerId);

        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO bank_accounts (id, player_id, balance) VALUES (?, ?, ?) " +
                "ON CONFLICT(player_id) DO UPDATE SET balance = ?")) {
          stmt.setString(1, UUID.randomUUID().toString());
          stmt.setString(2, playerId.toString());
          stmt.setLong(3, amount);
          stmt.setLong(4, amount);
          stmt.executeUpdate();
        }

        recordEntry(conn, playerId, LedgerEntryType.ADMIN_SET, amount - previousBalance, amount, null);
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      }
    }
  }

  private int credit(Connection conn, UUID playerId, int amount) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(
        "INSERT INTO bank_accounts (id, player_id, balance) VALUES (?, ?, ?) " +
            "ON CONFLICT(player_id) DO UPDATE SET balance = balance + ? " +
            "RETURNING balance")) {
      stmt.setString(1, UUID.randomUUID().toString());
      stmt.setString(2, playerId.toString());
      stmt.setLong(3, amount);
      stmt.setLong(4, amount);

      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        return rs.getInt("balance");
      }
    }
  }

  private Integer debit(Connection conn, UUID playerId, int amount) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(
        "UPDATE bank_accounts SET balance = balance - ? WHERE player_id = ? AND balance >= ? " +
            "RETURNING balance")) {
      stmt.setLong(1, amount);
      stmt.setString(2, playerId.toString());
      stmt.setLong(3, amount);

      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          return null;
        }
        return rs.getInt("balance");
      }
    }
  }

  private int readBalance(Connection conn, UUID playerId) throws SQLException {
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

  private void recordEntry(Connection conn, UUID playerId, LedgerEntryType type, int balanceChange,
      int balanceAfter, UUID targetPlayerId) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(
        "INSERT INTO bank_ledger (id, player_id, type, balance_change, balance_after, target_player_id, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
      stmt.setString(1, UUID.randomUUID().toString());
      stmt.setString(2, playerId.toString());
      stmt.setString(3, type.name());
      stmt.setInt(4, balanceChange);
      stmt.setInt(5, balanceAfter);
      stmt.setString(6, targetPlayerId == null ? null : targetPlayerId.toString());
      stmt.setLong(7, System.currentTimeMillis());
      stmt.executeUpdate();
    }
  }
}
