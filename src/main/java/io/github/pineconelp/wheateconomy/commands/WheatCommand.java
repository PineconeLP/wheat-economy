package io.github.pineconelp.wheateconomy.commands;

import java.time.Duration;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.github.pineconelp.wheateconomy.wheat.WheatGrowthSimulator;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class WheatCommand {
  private WheatGrowthSimulator wheatGrowthSimulator;

  public WheatCommand(WheatGrowthSimulator wheatGrowthSimulator) {
    super();

    this.wheatGrowthSimulator = wheatGrowthSimulator;
  }

  public LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("wheat")
        .requires((ctx) -> ctx.getSender().isOp())
        .then(Commands.literal("simulate")
            .then(Commands.argument("seconds", IntegerArgumentType.integer(1))
                .executes((ctx) -> {
                  CommandSender sender = ctx.getSource().getSender();

                  if (!(sender instanceof Player player)) {
                    sender.sendMessage("You are not a player.");
                    return Command.SINGLE_SUCCESS;
                  }

                  int seconds = ctx.getArgument("seconds", Integer.class);

                  wheatGrowthSimulator.simulate(
                      player.getChunk(),
                      Duration.ofSeconds(seconds));

                  return Command.SINGLE_SUCCESS;
                })))
        .build();
  }
}
