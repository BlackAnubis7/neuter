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
    private static CommandSyntaxException invalidIntException(String parsed) {
        return new SimpleCommandExceptionType(Text.of("Invalid whole number: " + parsed)).create();
    }

    private static void message(CommandContext<ServerCommandSource> context, Text text) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        player.sendMessage(text, false);
    }

    private static void message(CommandContext<ServerCommandSource> context, String text) throws CommandSyntaxException {
        message(context, Text.of(text));
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
                .append(new LiteralText(" -> "))
                .append(behaviour.toText());
    }

    private static MutableText customRuleAddText(Identifier entityId, BehaviourEnum behaviour) {
        return new LiteralText("Behaviour exception added: ")
                .append(customRuleText(entityId, behaviour));
    }

    private static MutableText customRuleRemoveText(Identifier entityId) {
        return new LiteralText("Behaviour exception removed: ")
                .append(translatedEntityName(entityId));
    }

    public static final LiteralArgumentBuilder<ServerCommandSource> configCommandsBuilder = literal("neuter")
            .then(
                literal("angry").executes(context -> {
                    message(context, "Anger time currently set to " + Neuter.getConfigAngerSeconds() + " seconds");
                    return Command.SINGLE_SUCCESS;
                }).then(
                    argument("seconds", word()).executes(context -> {
                        try {
                            int sec = Integer.parseInt(getString(context, "seconds"));
                            Neuter.setConfigAngerSeconds(sec);
                            message(context, "Anger time changed to " + sec + " seconds");
                            return Command.SINGLE_SUCCESS;
                        } catch (NumberFormatException e) {
                            throw invalidIntException(getString(context, "seconds"));
                        }
                    })
                ))
            .then(
                literal("help").executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    player.sendMessage(Text.of("No help available (for now)"), false);
                    return Command.SINGLE_SUCCESS;
                }))
            .then(
                literal("behaviour").executes(context -> {
                    message(context, new LiteralText("Default behaviour currently set to: ").append(Neuter.getConfigDefaultBehaviour().toText()));
                    return Command.SINGLE_SUCCESS;
                }).then(
                    literal("passive").executes(context -> {
                        Neuter.setConfigDefaultBehaviour(BehaviourEnum.PASSIVE);
                        message(context, new LiteralText("Default behaviour changed to: ").append(BehaviourEnum.PASSIVE.toText()));
                        return Command.SINGLE_SUCCESS;
                    })
                ).then(
                    literal("neutral").executes(context -> {
                        Neuter.setConfigDefaultBehaviour(BehaviourEnum.NEUTRAL);
                        message(context, new LiteralText("Default behaviour changed to: ").append(BehaviourEnum.NEUTRAL.toText()));
                        return Command.SINGLE_SUCCESS;
                    })
                ).then(
                    literal("hostile").executes(context -> {
                        Neuter.setConfigDefaultBehaviour(BehaviourEnum.HOSTILE);
                        message(context, new LiteralText("Default behaviour changed to: ").append(BehaviourEnum.HOSTILE.toText()));
                        return Command.SINGLE_SUCCESS;
                    })
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
                        message(context, new LiteralText("Behaviour exceptions reset to default"));
                        return Command.SINGLE_SUCCESS;
                    })
                ).then(
                    literal("add").then(
                        argument("entity", entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(
                            literal("passive").executes(context -> {
                                Identifier entityId = getEntitySummon(context, "entity");
                                Neuter.addConfigCustomRule(entityId.toString(), BehaviourEnum.PASSIVE);
                                message(context, customRuleAddText(entityId, BehaviourEnum.PASSIVE));
                                return Command.SINGLE_SUCCESS;
                            })
                        ).then(
                            literal("neutral").executes(context -> {
                                Identifier entityId = getEntitySummon(context, "entity");
                                Neuter.addConfigCustomRule(entityId.toString(), BehaviourEnum.NEUTRAL);
                                message(context, customRuleAddText(entityId, BehaviourEnum.NEUTRAL));
                                return Command.SINGLE_SUCCESS;
                            })
                        ).then(
                            literal("hostile").executes(context -> {
                                Identifier entityId = getEntitySummon(context, "entity");
                                Neuter.addConfigCustomRule(entityId.toString(), BehaviourEnum.HOSTILE);
                                message(context, customRuleAddText(entityId, BehaviourEnum.HOSTILE));
                                return Command.SINGLE_SUCCESS;
                            })
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
                        message(context, "Neuter settings reset to default");
                        return Command.SINGLE_SUCCESS;
                })
            );
}
