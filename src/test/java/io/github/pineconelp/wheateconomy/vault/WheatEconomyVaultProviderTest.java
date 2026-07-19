package io.github.pineconelp.wheateconomy.vault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import io.github.pineconelp.wheateconomy.bank.BankRepository;
import io.github.pineconelp.wheateconomy.support.EconomyTest;
import net.milkbowl.vault.economy.EconomyResponse;

class WheatEconomyVaultProviderTest extends EconomyTest {
  @Test
  void depositPlayer_storesWholeWheatAndFloorsFractions() throws Exception {
    PlayerMock player = addPlayer();
    WheatEconomyVaultProvider economy = new WheatEconomyVaultProvider(plugin, new BankRepository(testDataSource()));

    EconomyResponse response = economy.depositPlayer(player, 10.75);

    assertTrue(response.transactionSuccess());
    assertEquals(10, response.amount);
    assertEquals(10, response.balance);
    assertEquals(10, balanceOf(player.getUniqueId()));
  }

  @Test
  void withdrawPlayer_deductsWholeWheatAndRecordsLedgerEntry() throws Exception {
    PlayerMock player = addPlayer();
    seedBalance(player.getUniqueId(), 15);
    WheatEconomyVaultProvider economy = new WheatEconomyVaultProvider(plugin, new BankRepository(testDataSource()));

    EconomyResponse response = economy.withdrawPlayer(player, 4.99);

    assertTrue(response.transactionSuccess());
    assertEquals(4, response.amount);
    assertEquals(11, response.balance);
    assertEquals(11, balanceOf(player.getUniqueId()));
    assertEquals("WITHDRAW", ledgerOf(player.getUniqueId()).get(0).type());
  }

  @Test
  void withdrawPlayer_withInsufficientBalanceFailsWithoutChangingBalance() throws Exception {
    PlayerMock player = addPlayer();
    seedBalance(player.getUniqueId(), 3);
    WheatEconomyVaultProvider economy = new WheatEconomyVaultProvider(plugin, new BankRepository(testDataSource()));

    EconomyResponse response = economy.withdrawPlayer(player, 4);

    assertFalse(response.transactionSuccess());
    assertEquals(3, balanceOf(player.getUniqueId()));
    assertTrue(ledgerOf(player.getUniqueId()).isEmpty());
  }
}
