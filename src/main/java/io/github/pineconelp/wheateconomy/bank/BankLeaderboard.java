package io.github.pineconelp.wheateconomy.bank;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BankLeaderboard {
  private static final int TOP_ACCOUNT_LIMIT = 100;
  private static final int ENTRIES_PER_PAGE = 5;

  private final BankRepository bankRepository;
  private final Plugin plugin;

  public BankLeaderboard(BankRepository bankRepository, Plugin plugin) {
    this.bankRepository = bankRepository;
    this.plugin = plugin;
  }

  public void open(Player player) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      try {
        List<BankAccount> topAccounts = bankRepository.getTopAccounts(TOP_ACCOUNT_LIMIT);

        Bukkit.getScheduler().runTask(plugin, () -> openBook(player, topAccounts));
      } catch (SQLException e) {
        plugin.getLogger().log(Level.SEVERE, "Failed to load bank leaderboard", e);
        Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(
            Component.text("Failed to load leaderboard. Please try again later.", NamedTextColor.RED)));
      }
    });
  }

  private void openBook(Player player, List<BankAccount> topAccounts) {
    List<Component> pages = new ArrayList<>();

    TextComponent.Builder currentPage = newPage();
    int currentPageLines = 0;

    for (int rank = 0; rank < topAccounts.size(); rank++) {
      if (currentPageLines == ENTRIES_PER_PAGE) {
        pages.add(currentPage.build());

        currentPage = newPage();
        currentPageLines = 0;
      }

      BankAccount currentAccount = topAccounts.get(rank);

      currentPage.append(Component.text(
          (rank + 1) + ". " + currentAccount.getPlayerName() + "\n",
          NamedTextColor.BLACK));
      currentPage.append(Component.text(
          currentAccount.getBalance() + " Wheat\n",
          NamedTextColor.DARK_GRAY));
      currentPageLines++;
    }

    if (currentPageLines > 0) {
      pages.add(currentPage.build());
    }

    ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
    BookMeta meta = (BookMeta) book.getItemMeta();

    meta.title(Component.text("Bank Leaderboard"));
    meta.setAuthor("WheatEconomy");
    meta.pages(pages);

    book.setItemMeta(meta);
    player.openBook(book);
  }

  private TextComponent.Builder newPage() {
    TextComponent.Builder page = Component.text();
    page.append(Component.text("Bank Leaderboard\n", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    page.append(Component.text("\n"));
    return page;
  }
}
