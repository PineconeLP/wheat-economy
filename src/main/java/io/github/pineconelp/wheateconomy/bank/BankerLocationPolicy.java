package io.github.pineconelp.wheateconomy.bank;

import org.bukkit.entity.Player;

public interface BankerLocationPolicy {
  boolean isNearBanker(Player player);
}
