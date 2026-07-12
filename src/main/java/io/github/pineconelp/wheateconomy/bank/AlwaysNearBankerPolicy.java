package io.github.pineconelp.wheateconomy.bank;

import org.bukkit.entity.Player;

public class AlwaysNearBankerPolicy implements BankerLocationPolicy {
  @Override
  public boolean isNearBanker(Player player) {
    return true;
  }
}
