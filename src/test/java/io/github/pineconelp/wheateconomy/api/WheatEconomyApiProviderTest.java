package io.github.pineconelp.wheateconomy.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import io.github.pineconelp.wheateconomy.support.EconomyTest;

class WheatEconomyApiProviderTest extends EconomyTest {
  @Test
  void apiService_isRegisteredWhenPluginStarts() {
    RegisteredServiceProvider<WheatEconomyApi> registration = server.getServicesManager()
        .getRegistration(WheatEconomyApi.class);

    assertNotNull(registration);
    assertNotNull(registration.getProvider());
  }

  @Test
  void openBankSummary_usesTheRealBankBalance() throws Exception {
    PlayerMock player = addPlayer();
    seedBalance(player.getUniqueId(), 37);
    WheatEconomyApi api = server.getServicesManager().getRegistration(WheatEconomyApi.class).getProvider();

    api.openBankSummary(player);
    drainScheduler();

    assertTrue(player.nextMessage().contains("Bank"));
    assertTrue(player.nextMessage().contains("37"));
  }

  @Test
  void getWheatGrowthFlag_exposesTheWorldGuardFlagName() {
    WheatEconomyApi api = server.getServicesManager().getRegistration(WheatEconomyApi.class).getProvider();

    assertEquals("wheat-growth", api.getWheatGrowthFlag().getName());
  }
}
