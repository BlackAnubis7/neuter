package net.anubis.neuter.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class ConfigCommands {
    private static CommandSyntaxException invalidIntException(String parsed) {
        return new SimpleCommandExceptionType(Text.of("Invalid whole number: " + parsed)).create();
    }

    public static final LiteralArgumentBuilder<ServerCommandSource> configCommandsBuilder = literal("neuter").then(
            literal("angry").then(
                argument("seconds", word()).executes(context -> {
                    try {
                        int sec = Integer.parseInt(getString(context, "seconds"));
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        player.sendMessage(Text.of("Anger set to " + sec + " seconds"), false);
                        return Command.SINGLE_SUCCESS;
                    } catch (NumberFormatException e) {
                        throw invalidIntException(getString(context, "seconds"));
                    }
                })
            )).then(
            literal("help").executes(context -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                player.sendMessage(Text.of("No help available (for now)"), false);
                return Command.SINGLE_SUCCESS;
            }));
}
