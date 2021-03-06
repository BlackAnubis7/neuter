package net.anubis.neuter.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.anubis.neuter.Neuter;
import net.anubis.neuter.config.BehaviourEnum;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;
import java.util.Optional;

import static net.minecraft.command.argument.EntitySummonArgumentType.entitySummon;
import static net.minecraft.command.argument.EntitySummonArgumentType.getEntitySummon;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class ConfigCommands {
    private final static String separator = ": ";
    private final static String changeSeparator = " -> ";

    private static CommandSyntaxException invalidIntException(String parsed) {
        return new SimpleCommandExceptionType(new TranslatableText("command.neuter.error.int").append(parsed)).create();
    }

    private static void message(CommandContext<ServerCommandSource> context, Text text) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        player.sendMessage(text, false);
    }

    private static MutableText translatedEntityName(Identifier entityId) {
        Optional<EntityType<?>> entityType = Registry.ENTITY_TYPE.getOrEmpty(entityId);
        MutableText entityName;
        if (entityType.isPresent()) {
            entityName = new TranslatableText(entityType.get().getTranslationKey());
        } else {
            entityName = new LiteralText(entityId.toString());
        }
        return entityName;
    }

    private static MutableText customRuleText(Identifier entityId, BehaviourEnum behaviour) {
        return translatedEntityName(entityId)
                .append(changeSeparator)
                .append(behaviour.toText());
    }

    private static MutableText customRuleAddText(Identifier entityId, BehaviourEnum behaviour) {
        return new TranslatableText("command.neuter.exception.add")
                .append(separator)
                .append(customRuleText(entityId, behaviour));
    }

    private static MutableText customRuleRemoveText(Identifier entityId) {
        return new TranslatableText("command.neuter.exception.remove")
                .append(separator)
                .append(translatedEntityName(entityId));
    }

    public static final LiteralArgumentBuilder<ServerCommandSource> configCommandsBuilder = literal("neuter")
            .then(
                literal("angry").executes(context -> {
                    message(context, new TranslatableText("command.neuter.anger.current")
                            .append(" " + Neuter.getConfigAngerSeconds() + " ")
                            .append(new TranslatableText("command.neuter.anger.seconds"))
                    );
                    return Command.SINGLE_SUCCESS;
                }).then(
                    argument("seconds", word()).executes(context -> {
                        try {
                            int sec = Integer.parseInt(getString(context, "seconds"));
                            Neuter.setConfigAngerSeconds(sec);
                            message(context, new TranslatableText("command.neuter.anger.change")
                                    .append(" " + sec + " ")
                                    .append(new TranslatableText("command.neuter.anger.seconds"))
                            );
                            return Command.SINGLE_SUCCESS;
                        } catch (NumberFormatException e) {
                            throw invalidIntException(getString(context, "seconds"));
                        }
                    })
                ))
            .then(
                literal("help").executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    helpAction(player);
                    return Command.SINGLE_SUCCESS;
                }))
            .then(
                literal("behaviour").executes(context -> {
                    message(context, new TranslatableText("command.neuter.behaviour.current")
                            .append(separator)
                            .append(Neuter.getConfigDefaultBehaviour().toText()));
                    return Command.SINGLE_SUCCESS;
                }).then(
                    literal("passive").executes(context -> behaviourChangeAction(context, BehaviourEnum.PASSIVE))
                ).then(
                    literal("neutral").executes(context -> behaviourChangeAction(context, BehaviourEnum.NEUTRAL))
                ).then(
                    literal("hostile").executes(context -> behaviourChangeAction(context, BehaviourEnum.HOSTILE))
                ))
            .then(
                literal("exception").then(
                    literal("list").executes(context -> {
                        for (Map.Entry<String, BehaviourEnum> cr : Neuter.getConfigCustomRules().entrySet()) {
                            message(context, customRuleText(new Identifier(cr.getKey()), cr.getValue()));
                        }
                        return Command.SINGLE_SUCCESS;
                    })
                ).then(
                    literal("reset").executes(context -> {
                        Neuter.resetConfigCustomRule();
                        message(context, new TranslatableText("command.neuter.behaviour.reset"));
                        return Command.SINGLE_SUCCESS;
                    })
                ).then(
                    literal("add").then(
                        argument("entity", entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(
                            literal("passive").executes(context -> ruleAddAction(context, BehaviourEnum.PASSIVE))
                        ).then(
                            literal("neutral").executes(context -> ruleAddAction(context, BehaviourEnum.NEUTRAL))
                        ).then(
                            literal("hostile").executes(context -> ruleAddAction(context, BehaviourEnum.HOSTILE))
                        )
                    )
                ).then(
                    literal("remove").then(
                        argument("entity", entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes(context -> {
                            Identifier entityId = getEntitySummon(context, "entity");
                            Neuter.removeConfigCustomRule(entityId.toString());
                            message(context, customRuleRemoveText(entityId));
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
            ).then(
                literal("default").executes(context -> {
                        Neuter.defaultConfig();
                        message(context, new TranslatableText("command.neuter.reset"));
                        return Command.SINGLE_SUCCESS;
                })
            );

    private static void helpAction(ServerPlayerEntity player) {
        player.sendMessage(new TranslatableText("command.neuter.help.nohelp"), false);
    }

    private static int behaviourChangeAction(CommandContext<ServerCommandSource> context, BehaviourEnum newBehaviour) throws CommandSyntaxException {
        Neuter.setConfigDefaultBehaviour(newBehaviour);
        message(context, new TranslatableText("command.neuter.behaviour.change").append(separator).append(newBehaviour.toText()));
        return Command.SINGLE_SUCCESS;
    }

    private static int ruleAddAction(CommandContext<ServerCommandSource> context, BehaviourEnum newBehaviour) throws CommandSyntaxException {
        Identifier entityId = getEntitySummon(context, "entity");
        Neuter.addConfigCustomRule(entityId.toString(), newBehaviour);
        message(context, customRuleAddText(entityId, newBehaviour));
        return Command.SINGLE_SUCCESS;
    }
}
