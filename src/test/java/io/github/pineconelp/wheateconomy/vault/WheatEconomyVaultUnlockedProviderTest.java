package io.github.pineconelp.wheateconomy.vault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import io.github.pineconelp.wheateconomy.bank.BankRepository;
import io.github.pineconelp.wheateconomy.support.EconomyTest;
import net.milkbowl.vault2.economy.EconomyResponse;
import net.milkbowl.vault2.economy.MultiEconomyResponse;

class WheatEconomyVaultUnlockedProviderTest extends EconomyTest {
  @Test
  void deposit_rejectsUnknownCurrencyWithoutChangingBalance() throws Exception {
    PlayerMock player = addPlayer();
    WheatEconomyVaultUnlockedProvider economy = new WheatEconomyVaultUnlockedProvider(
        plugin,
        new BankRepository(testDataSource()));

    EconomyResponse response = economy.deposit("ShopPlugin", player.getUniqueId(), "world", "Coins", BigDecimal.TEN);

    assertFalse(response.transactionSuccess());
    assertEquals(0, balanceOf(player.getUniqueId()));
    assertTrue(ledgerOf(player.getUniqueId()).isEmpty());
  }

  @Test
  void set_overwritesBalanceThroughVaultUnlockedInterface() throws Exception {
    PlayerMock player = addPlayer();
    seedBalance(player.getUniqueId(), 4);
    WheatEconomyVaultUnlockedProvider economy = new WheatEconomyVaultUnlockedProvider(
        plugin,
        new BankRepository(testDataSource()));

    EconomyResponse response = economy.set("ShopPlugin", player.getUniqueId(), BigDecimal.valueOf(19.9));

    assertTrue(response.transactionSuccess());
    assertEquals(BigDecimal.valueOf(19), response.amount);
    assertEquals(19, balanceOf(player.getUniqueId()));
  }

  @Test
  void transfer_movesWheatBetweenAccountsAndReturnsBothBalances() throws Exception {
    PlayerMock sender = server.addPlayer("Alice");
    PlayerMock target = server.addPlayer("Bob");
    seedBalance(sender.getUniqueId(), 30);
    WheatEconomyVaultUnlockedProvider economy = new WheatEconomyVaultUnlockedProvider(
        plugin,
        new BankRepository(testDataSource()));

    MultiEconomyResponse response = economy.transfer(
        "ShopPlugin",
        sender.getUniqueId(),
        target.getUniqueId(),
        BigDecimal.valueOf(12));

    assertEquals(EconomyResponse.ResponseType.SUCCESS, response.type());
    assertEquals(BigDecimal.valueOf(18), response.balance(sender.getUniqueId()).orElseThrow());
    assertEquals(BigDecimal.valueOf(12), response.balance(target.getUniqueId()).orElseThrow());
    assertEquals(18, balanceOf(sender.getUniqueId()));
    assertEquals(12, balanceOf(target.getUniqueId()));
  }
}
