package net.idothehax.theoldbroadcast.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.idothehax.theoldbroadcast.SanityHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

import java.util.Collection;

public class SanityCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sanity")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("set")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("value", IntegerArgumentType.integer(0, SanityHandler.MAX_SANITY))
                        .executes(SanityCommand::setSanity))))
            .then(Commands.literal("add")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("value", IntegerArgumentType.integer())
                        .executes(SanityCommand::addSanity))))
            .then(Commands.literal("get")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(SanityCommand::getSanity)))
            .then(Commands.literal("reset")
                .then(Commands.argument("targets", EntityArgument.players())
                    .executes(SanityCommand::resetSanity))));
    }

    private static int setSanity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
        int value = IntegerArgumentType.getInteger(context, "value");

        for (ServerPlayer player : targets) {
            player.getPersistentData().putInt(SanityHandler.SANITY_TAG, value);
        }

        if (targets.size() == 1) {
            context.getSource().sendSuccess(() -> Component.literal("Set sanity to " + value + " for " + targets.iterator().next().getName().getString()), true);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("Set sanity to " + value + " for " + targets.size() + " players"), true);
        }

        return targets.size();
    }

    private static int addSanity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
        int value = IntegerArgumentType.getInteger(context, "value");

        for (ServerPlayer player : targets) {
            int currentSanity = player.getPersistentData().getInt(SanityHandler.SANITY_TAG);
            int newSanity = Mth.clamp(currentSanity + value, 0, SanityHandler.MAX_SANITY);
            player.getPersistentData().putInt(SanityHandler.SANITY_TAG, newSanity);
        }

        String operation = value >= 0 ? "Added " + value : "Removed " + Math.abs(value);
        if (targets.size() == 1) {
            context.getSource().sendSuccess(() -> Component.literal(operation + " sanity for " + targets.iterator().next().getName().getString()), true);
        } else {
            context.getSource().sendSuccess(() -> Component.literal(operation + " sanity for " + targets.size() + " players"), true);
        }

        return targets.size();
    }

    private static int getSanity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        int sanity = target.getPersistentData().getInt(SanityHandler.SANITY_TAG);

        context.getSource().sendSuccess(() -> Component.literal(target.getName().getString() + " has " + sanity + "/" + SanityHandler.MAX_SANITY + " sanity"), false);

        return sanity;
    }

    private static int resetSanity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");

        for (ServerPlayer player : targets) {
            player.getPersistentData().putInt(SanityHandler.SANITY_TAG, SanityHandler.MAX_SANITY);
        }

        if (targets.size() == 1) {
            context.getSource().sendSuccess(() -> Component.literal("Reset sanity for " + targets.iterator().next().getName().getString()), true);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("Reset sanity for " + targets.size() + " players"), true);
        }

        return targets.size();
    }
}
