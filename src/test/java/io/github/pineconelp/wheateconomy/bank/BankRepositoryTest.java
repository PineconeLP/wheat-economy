package io.github.pineconelp.wheateconomy.bank;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github.pineconelp.wheateconomy.support.EconomyTest;

class BankRepositoryTest extends EconomyTest {
  @Test
  void getTopAccounts_ordersByBalanceDescending() throws SQLException {
    UUID richest = UUID.randomUUID();
    UUID middle = UUID.randomUUID();
    UUID poorest = UUID.randomUUID();

    seedBalance(middle, 500);
    seedBalance(richest, 1000);
    seedBalance(poorest, 10);

    List<BankAccount> top = new BankRepository(testDataSource()).getTopAccounts(10);

    assertEquals(3, top.size());
    assertEquals(richest, top.get(0).getPlayerId());
    assertEquals(1000, top.get(0).getBalance());
    assertEquals(middle, top.get(1).getPlayerId());
    assertEquals(poorest, top.get(2).getPlayerId());
  }

  @Test
  void getTopAccounts_respectsLimit() throws SQLException {
    for (int i = 0; i < 5; i++) {
      seedBalance(UUID.randomUUID(), (i + 1) * 100);
    }

    List<BankAccount> top = new BankRepository(testDataSource()).getTopAccounts(3);

    assertEquals(3, top.size());
    assertEquals(500, top.get(0).getBalance());
    assertEquals(400, top.get(1).getBalance());
    assertEquals(300, top.get(2).getBalance());
  }
}
