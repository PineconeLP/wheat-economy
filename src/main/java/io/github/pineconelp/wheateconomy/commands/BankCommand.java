package io.github.pineconelp.wheateconomy.commands;

import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pineconelp.wheateconomy.bank.Bank;
import io.github.pineconelp.wheateconomy.bank.BankLeaderboard;
import io.github.pineconelp.wheateconomy.bank.BankerLocationPolicy;
import io.github.pineconelp.wheateconomy.bank.deposit.DepositAllHayBalesStrategy;
import io.github.pineconelp.wheateconomy.bank.deposit.DepositAllStrategy;
import io.github.pineconelp.wheateconomy.bank.deposit.DepositAllWheatStrategy;
import io.github.pineconelp.wheateconomy.bank.deposit.DepositHayBaleAmountStrategy;
import io.github.pineconelp.wheateconomy.bank.deposit.DepositWheatAmountStrategy;
import io.github.pineconelp.wheateconomy.bank.withdraw.WithdrawAllHayBalesStrategy;
import io.github.pineconelp.wheateconomy.bank.withdraw.WithdrawAllWheatStrategy;
import io.github.pineconelp.wheateconomy.bank.withdraw.WithdrawHayBaleAmountStrategy;
import io.github.pineconelp.wheateconomy.bank.withdraw.WithdrawWheatAmountStrategy;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BankCommand {
  private Bank bank;
  private BankLeaderboard bankLeaderboard;
  private BankerLocationPolicy locationPolicy;

  public BankCommand(Bank bank, BankLeaderboard bankLeaderboard, BankerLocationPolicy locationPolicy) {
    super();

    this.bank = bank;
    this.bankLeaderboard = bankLeaderboard;
    this.locationPolicy = locationPolicy;
  }

  public LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("bank")
        .then(createLeaderboardCommand())
        .then(createBalanceCommand())
        .then(createDepositCommand())
        .then(createWithdrawCommand())
        .then(createSendCommand())
        .then(createGetCommand())
        .then(createSetCommand())
        .then(createAddCommand())
        .build();
  }

  private LiteralArgumentBuilder<CommandSourceStack> createLeaderboardCommand() {
    return Commands.literal("leaderboard")
        .requires(ctx -> ctx.getSender().hasPermission("wheateconomy.bank"))
        .executes((ctx) -> {
          CommandSender sender = ctx.getSource().getSender();

          if (notifyNotPlayer(sender)) {
            return Command.SINGLE_SUCCESS;
          }

          bankLeaderboard.open((Player) sender);

          return Command.SINGLE_SUCCESS;
        });
  }

  private LiteralArgumentBuilder<CommandSourceStack> createBalanceCommand() {
    return Commands.literal("balance")
        .requires(ctx -> ctx.getSender().hasPermission("wheateconomy.bank"))
        .executes((ctx) -> {
          CommandSender sender = ctx.getSource().getSender();

          if (notifyNotPlayer(sender)) {
            return Command.SINGLE_SUCCESS;
          }

          Player player = (Player) sender;

          if (locationPolicy.isNearBanker(player)) {
            bank.showSummary(player);
          } else {
            bank.showBalance(player);
          }

          return Command.SINGLE_SUCCESS;
        });
  }

  private LiteralArgumentBuilder<CommandSourceStack> createDepositCommand() {
    return Commands.literal("deposit")
        .requires(ctx -> ctx.getSender().hasPermission("wheateconomy.bank"))
        .then(Commands.literal("all").executes((ctx) -> {
          if (!notifyCannotRunCommand(ctx)) {
            Player player = (Player) ctx.getSource().getSender();

            bank.deposit(player, new DepositAllStrategy());
          }

          return Command.SINGLE_SUCCESS;
        }))
        .then(Commands.literal("wheat")
            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .suggests((ctx, builder) -> {
                  builder.suggest("<amount>");

                  return builder.buildFuture();
                })
                .executes((ctx) -> {
                  if (!notifyCannotRunCommand(ctx)) {
                    Player player = (Player) ctx.getSource().getSender();

                    int amount = ctx.getArgument("amount", Integer.class);

                    bank.deposit(player, new DepositWheatAmountStrategy(amount));
                  }

                  return Command.SINGLE_SUCCESS;
                }))
            .then(Commands.literal("all").executes((ctx) -> {
              if (!notifyCannotRunCommand(ctx)) {
                Player player = (Player) ctx.getSource().getSender();

                bank.deposit(player, new DepositAllWheatStrategy());
              }

              return Command.SINGLE_SUCCESS;
            })))
        .then(Commands.literal("haybale")
            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .suggests((ctx, builder) -> {
                  builder.suggest("<amount>");

                  return builder.buildFuture();
                })
                .executes((ctx) -> {
                  if (!notifyCannotRunCommand(ctx)) {
                    Player player = (Player) ctx.getSource().getSender();

                    int amount = ctx.getArgument("amount", Integer.class);

                    bank.deposit(player, new DepositHayBaleAmountStrategy(amount));
                  }

                  return Command.SINGLE_SUCCESS;
                }))
            .then(Commands.literal("all").executes((ctx) -> {
              if (!notifyCannotRunCommand(ctx)) {
                Player player = (Player) ctx.getSource().getSender();

                bank.deposit(player, new DepositAllHayBalesStrategy());
              }

              return Command.SINGLE_SUCCESS;
            })));
  }

  private LiteralArgumentBuilder<CommandSourceStack> createWithdrawCommand() {
    return Commands.literal("withdraw")
        .requires(ctx -> ctx.getSender().hasPermission("wheateconomy.bank"))
        .then(Commands.literal("wheat")
            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .suggests((ctx, builder) -> {
                  builder.suggest("<amount>");

                  return builder.buildFuture();
                })
                .executes((ctx) -> {
                  if (!notifyCannotRunCommand(ctx)) {
                    Player player = (Player) ctx.getSource().getSender();

                    int amount = ctx.getArgument("amount", Integer.class);

                    bank.withdraw(player, new WithdrawWheatAmountStrategy(amount));
                  }

                  return Command.SINGLE_SUCCESS;
                }))
            .then(Commands.literal("all").executes((ctx) -> {
              if (!notifyCannotRunCommand(ctx)) {
                Player player = (Player) ctx.getSource().getSender();

                bank.withdraw(player, new WithdrawAllWheatStrategy());
              }

              return Command.SINGLE_SUCCESS;
            })))
        .then(Commands.literal("haybale")
            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .suggests((ctx, builder) -> {
                  builder.suggest("<amount>");

                  return builder.buildFuture();
                })
                .executes((ctx) -> {
                  if (!notifyCannotRunCommand(ctx)) {
                    Player player = (Player) ctx.getSource().getSender();

                    int amount = ctx.getArgument("amount", Integer.class);

                    bank.withdraw(player, new WithdrawHayBaleAmountStrategy(amount));
                  }

                  return Command.SINGLE_SUCCESS;
                }))
            .then(Commands.literal("all").executes((ctx) -> {
              if (!notifyCannotRunCommand(ctx)) {
                Player player = (Player) ctx.getSource().getSender();

                bank.withdraw(player, new WithdrawAllHayBalesStrategy());
              }

              return Command.SINGLE_SUCCESS;
            })));
  }

  private LiteralArgumentBuilder<CommandSourceStack> createSendCommand() {
    return Commands.literal("send")
        .requires(ctx -> ctx.getSender().hasPermission("wheateconomy.bank"))
        .then(Commands.argument("target", StringArgumentType.string())
            .suggests(suggestOnlinePlayerNames())
            .then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(
                (ctx) -> {
                  if (!notifyCannotRunCommand(ctx)) {
                    Player player = (Player) ctx.getSource().getSender();

                    String target = ctx.getArgument("target", String.class);
                    int amount = ctx.getArgument("amount", Integer.class);

                    bank.send(player, target, amount);
                  }

                  return Command.SINGLE_SUCCESS;
                })));
  }

  private LiteralArgumentBuilder<CommandSourceStack> createGetCommand() {
    return Commands.literal("get")
        .requires(ctx -> ctx.getSender().hasPermission("wheateconomy.bank.admin"))
        .then(Commands.argument("target", StringArgumentType.string())
            .suggests(suggestOnlinePlayerNames()).executes(
                (ctx) -> {
                  CommandSender sender = ctx.getSource().getSender();

                  String target = ctx.getArgument("target", String.class);
                  bank.getPlayerBalance(sender, target);

                  return Command.SINGLE_SUCCESS;
                }));
  }

  private LiteralArgumentBuilder<CommandSourceStack> createSetCommand() {
    return Commands.literal("set")
        .requires(ctx -> ctx.getSender().hasPermission("wheateconomy.bank.admin"))
        .then(Commands.argument("target", StringArgumentType.string())
            .suggests(suggestOnlinePlayerNames())
            .then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes(
                (ctx) -> {
                  CommandSender sender = ctx.getSource().getSender();

                  String target = ctx.getArgument("target", String.class);
                  int amount = ctx.getArgument("amount", Integer.class);

                  bank.setPlayerBalance(sender, target, amount);

                  return Command.SINGLE_SUCCESS;
                })));
  }

  private LiteralArgumentBuilder<CommandSourceStack> createAddCommand() {
    return Commands.literal("add")
        .requires(ctx -> ctx.getSender().hasPermission("wheateconomy.bank.admin"))
        .then(Commands.argument("target", StringArgumentType.string())
            .suggests(suggestOnlinePlayerNames())
            .then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(
                (ctx) -> {
                  CommandSender sender = ctx.getSource().getSender();

                  String target = ctx.getArgument("target", String.class);
                  int amount = ctx.getArgument("amount", Integer.class);

                  bank.addPlayerBalance(sender, target, amount);

                  return Command.SINGLE_SUCCESS;
                })));
  }

  private boolean notifyCannotRunCommand(CommandContext<CommandSourceStack> ctx) {
    CommandSender sender = ctx.getSource().getSender();

    if (notifyNotPlayer(sender)) {
      return true;
    }

    Player player = (Player) sender;

    if (!locationPolicy.isNearBanker(player)) {
      player.sendMessage(
          Component.text("You must be near a banker to interact with the bank.", NamedTextColor.RED));
      return true;
    }

    return false;
  }

  private boolean notifyNotPlayer(CommandSender sender) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(
          Component.text("You are not a player.", NamedTextColor.RED));
      return true;
    }

    return false;
  }

  private SuggestionProvider<CommandSourceStack> suggestOnlinePlayerNames() {
    return (ctx, builder) -> {
      Bukkit.getOnlinePlayers().stream()
          .map(Player::getName)
          .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase()))
          .forEach(builder::suggest);
      return builder.buildFuture();
    };
  }
}
