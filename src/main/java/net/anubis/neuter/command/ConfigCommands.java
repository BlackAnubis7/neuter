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
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
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
        return new SimpleCommandExceptionType(MutableText.of(new TranslatableTextContent("command.neuter.error.int")).append(parsed)).create();
    }

    private static void message(CommandContext<ServerCommandSource> context, Text text) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        player.sendMessage(text, false);
    }

    private static MutableText translatedEntityName(Identifier entityId) {
        Optional<EntityType<?>> entityType = Registry.ENTITY_TYPE.getOrEmpty(entityId);
        MutableText entityName;
        entityName = entityType.map(type -> MutableText.of(new TranslatableTextContent(type.getTranslationKey()))).orElseGet(() -> MutableText.of(new LiteralTextContent(entityId.toString())));
        return entityName;
    }

    private static MutableText customRuleText(Identifier entityId, BehaviourEnum behaviour) {
        MutableText ret = translatedEntityName(entityId)
                .append(changeSeparator)
                .append(behaviour.toText());
        if (Neuter.permanentBehaviour(entityId).isPresent()) return ret
                .append(MutableText.of(new TranslatableTextContent("command.neuter.exception.permanent")));
        else return ret;
    }

    private static MutableText customRuleAddText(Identifier entityId, BehaviourEnum behaviour) {
        return MutableText.of(new TranslatableTextContent("command.neuter.exception.add"))
                .append(separator)
                .append(customRuleText(entityId, behaviour));
    }

    private static MutableText customRuleRemoveText(Identifier entityId) {
        return MutableText.of(new TranslatableTextContent("command.neuter.exception.remove"))
                .append(separator)
                .append(translatedEntityName(entityId));
    }

    private static MutableText customRuleCantModifyText(Identifier entityId) {
        return translatedEntityName(entityId)
                .append(separator)
                .append(MutableText.of(new TranslatableTextContent("command.neuter.exception.cant_add")));
    }

    // Added for compatibility, might turn deprecated in the future
    private static MutableText customRuleCantModifyText(Identifier entityId, BehaviourEnum behaviour) {
        return customRuleCantModifyText(entityId);
    }

    public static final LiteralArgumentBuilder<ServerCommandSource> configCommandsBuilder = literal("neuter")
            .then(
                literal("angry").executes(context -> {
                    message(context, MutableText.of(new TranslatableTextContent("command.neuter.anger.current"))
                            .append(" " + Neuter.getConfigAngerSeconds() + " ")
                            .append(MutableText.of(new TranslatableTextContent("command.neuter.anger.seconds")))
                    );
                    return Command.SINGLE_SUCCESS;
                }).then(
                    argument("seconds", word()).executes(context -> {
                        try {
                            int sec = Integer.parseInt(getString(context, "seconds"));
                            Neuter.setConfigAngerSeconds(sec);
                            message(context, MutableText.of(new TranslatableTextContent("command.neuter.anger.change"))
                                    .append(" " + sec + " ")
                                    .append(MutableText.of(new TranslatableTextContent("command.neuter.anger.seconds")))
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
                    message(context, MutableText.of(new TranslatableTextContent("command.neuter.behaviour.current"))
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
                        message(context, MutableText.of(new TranslatableTextContent("command.neuter.behaviour.reset")));
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
                            Optional<BehaviourEnum> perm = Neuter.permanentBehaviour(entityId);

                            if (perm.isPresent()) {
                                message(context, customRuleCantModifyText(entityId));
                            } else {
                                Neuter.removeConfigCustomRule(entityId.toString());
                                message(context, customRuleRemoveText(entityId));
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
            ).then(
                literal("default").executes(context -> {
                        Neuter.defaultConfig();
                        message(context, MutableText.of(new TranslatableTextContent("command.neuter.reset")));
                        return Command.SINGLE_SUCCESS;
                })
            );

    private static void helpAction(ServerPlayerEntity player) {
        player.sendMessage(MutableText.of(new TranslatableTextContent("command.neuter.help.nohelp")), false);
    }

    private static int behaviourChangeAction(CommandContext<ServerCommandSource> context, BehaviourEnum newBehaviour) throws CommandSyntaxException {
        Neuter.setConfigDefaultBehaviour(newBehaviour);
        message(context, MutableText.of(new TranslatableTextContent("command.neuter.behaviour.change")).append(separator).append(newBehaviour.toText()));
        return Command.SINGLE_SUCCESS;
    }

    private static int ruleAddAction(CommandContext<ServerCommandSource> context, BehaviourEnum newBehaviour) throws CommandSyntaxException {
        Identifier entityId = getEntitySummon(context, "entity");
        Optional<BehaviourEnum> perm = Neuter.permanentBehaviour(entityId);

        if (perm.isPresent()) {
            message(context, customRuleCantModifyText(entityId, newBehaviour));
        } else {
            Neuter.addConfigCustomRule(entityId.toString(), newBehaviour);
            message(context, customRuleAddText(entityId, newBehaviour));
        }
        return Command.SINGLE_SUCCESS;
    }

//    /**
//     * Checks if a mob is invulnerable to lowering its hostility using Neuter as an unplanned behaviour. That excludes
//     * mobs that can never become aggressive (like a cow). Data might be incomplete.
//     * @param entityId entity being checked
//     * @return `true` if mob's hostility cannot be lowered by Neuter
//     */
//    private static boolean hostileDespiteNeuter(Identifier entityId) {
//        String[] affected = {"minecraft:warden", "minecraft:wither"};
//        return Arrays.stream(affected).anyMatch((mob) -> mob.equals(entityId.toString()));
//    }
}
