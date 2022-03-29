package net.anubis.neuter.config;

import net.anubis.neuter.Neuter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents set of Neuter configurations
 */
public class Config {
    private final Map<String, BehaviourEnum> customRules = new HashMap<>();
    private BehaviourEnum defaultBehaviour = BehaviourEnum.HOSTILE;
    private int angryTicks = 200;

    /**
     * Creates a Config instance with default values
     */
    public Config() {
        // pass
    }

    /**
     * Creates a Config instance with given default behaviour
     * @param defaultBehaviour default behaviour to be set
     */
    public Config(BehaviourEnum defaultBehaviour) {
        this.setDefaultBehaviour(defaultBehaviour);
    }

    /**
     * Creates a Config instance, which is a copy of another instance
     * @param otherConfig Config instance to be copied
     */
    public Config(Config otherConfig) {
        copyFrom(otherConfig);
    }

    /**
     * @deprecated
     * Resets custom entity rules to default values - for now it's hardcoded
     */
    @Deprecated public void defaultCustomRules() {
        this.customRules.clear();
        this.customRules.put("minecraft:ender_dragon", BehaviourEnum.HOSTILE);
    }

    /**
     * Removes all custom rules
     */
    public void clearCustomRules() {
        this.customRules.clear();
    }

    /**
     * Adds a custom behaviour role
     * @param entityIdentifier string identifier of entity type (for example <code>minecraft:creeper</code>)
     * @param rule behaviour meant for the given entity
     */
    public void addCustomRule(String entityIdentifier, BehaviourEnum rule) {
        this.customRules.put(entityIdentifier, rule);
    }

    public void removeCustomRule(String entityIdentifier) {
        this.customRules.remove(entityIdentifier);
    }

    /**
     * @deprecated
     * Resets default entity behaviour to default value - for now it's hardcoded
     */
    @Deprecated public void defaultDefaultBehaviour() {
        this.defaultBehaviour = BehaviourEnum.NEUTRAL;
    }

    /**
     * Copies custom rules from a different Config instance
     * @param otherConfig instance from which the rules are to be copied
     */
    public void copyCustomRules(Config otherConfig) {
        this.customRules.clear();
        this.customRules.putAll(otherConfig.getCustomRules());
    }

    /**
     * Copies all data from a different Config instance
     * @param otherConfig instance to be copied
     */
    public void copyFrom(Config otherConfig) {
        if(this != otherConfig) {
            copyCustomRules(otherConfig);
            this.defaultBehaviour = otherConfig.getDefaultBehaviour();
            this.angryTicks = otherConfig.getAngryTicks();
        }
    }

    /**
     * Get planned behaviour of a particular entity type
     * @param identifier string identifier of entity type (for example <code>minecraft:creeper</code>)
     * @return planned behaviour of given entity type
     */
    public BehaviourEnum entityBehaviour(String identifier) {
        return this.customRules.getOrDefault(identifier, this.defaultBehaviour);
    }

    /**
     * Get planned behaviour of a particular entity type
     * @param identifier {@link Identifier} instance of entity type
     * @return planned behaviour of given entity type
     */
    public BehaviourEnum entityBehaviour(Identifier identifier) {
        return entityBehaviour(identifier.toString());
    }

    /**
     * <b>UNTESTED - NEEDS MINECRAFT RUNTIME REGISTRY</b><br>
     * Get planned behaviour of a particular entity type
     * @param entityType {@link EntityType} instance
     * @return planned behaviour of given entity type
     */
    public BehaviourEnum entityBehaviour(EntityType<?> entityType) {
        return entityBehaviour(EntityType.getId(entityType));
    }

    /**
     * <b>UNTESTED - NEEDS MINECRAFT RUNTIME REGISTRY</b><br>
     * Get planned behaviour of a particular entity type
     * @param entity {@link Entity} instance
     * @return planned behaviour of given entity
     */
    public BehaviourEnum entityBehaviour(Entity entity) {
        return entityBehaviour(entity.getType());
    }

    public Map<String, BehaviourEnum> getCustomRules() {
        return customRules;
    }

    public BehaviourEnum getDefaultBehaviour() {
        return defaultBehaviour;
    }

    public int getAngryTicks() {
        return angryTicks;
    }

    /**
     * Sets the behaviour of entities that have no custom configuration
     * @param defaultBehaviour default behaviour to be set
     */
    public void setDefaultBehaviour(BehaviourEnum defaultBehaviour) {
        this.defaultBehaviour = defaultBehaviour;
    }

    public void setAngryTicks(int angryTicks) {
        this.angryTicks = angryTicks;
    }

    public void setAngrySeconds(int angrySeconds) {
        this.setAngryTicks(angrySeconds * Neuter.TICKS_IN_SECOND);
    }

    /**
     * Fills this Config instance with data from {@link NbtCompound} instance. Data which is
     * absent in the NBT will be defaulted. Keys used:
     * <ul><li><b>DefaultBehaviour</b> - <code>int</code> representation of mobs' default behaviour</li>
     * <li><b>AngryTicks</b> - number of ticks after which mobs forgive</li>
     * <li><b>CustomRules</b> - list of following pairs:<ul>
     *     <li><b>EntityIdentifier</b> - <code>string</code> identifier of the entity type (like <code>minecraft:creeper</code>)</li>
     *     <li><b>Behaviour</b> - <code>int</code> representation of entity's custom behaviour</li>
     * </ul></li></ul>
     * @param nbt Neuter config data (usually obtained by {@link PersistanceHelper})
     * @param defaultConfig default configuration for data absent in NBT
     */
    public void fromNbt(NbtCompound nbt, Config defaultConfig) {
        if (nbt.contains("DefaultBehaviour", 3)) {
            this.setDefaultBehaviour(BehaviourEnum.fromInt(nbt.getInt("DefaultBehaviour")));
        } else {
            this.setDefaultBehaviour(defaultConfig.getDefaultBehaviour());
        }
        if (nbt.contains("AngryTicks", 3)) {
            this.setAngryTicks(nbt.getInt("AngryTicks"));
        } else {
            this.setAngryTicks(defaultConfig.getAngryTicks());
        }
        if (nbt.contains("CustomRules", 9)) {
            this.clearCustomRules();
            NbtList rules = nbt.getList("CustomRules", 10);
            String entityId;
            int behaviour;
            for (int i = 0; i < rules.size(); i++) {
                entityId = rules.getCompound(i).getString("EntityIdentifier");
                behaviour = rules.getCompound(i).getInt("Behaviour");
                if (!Objects.equals(entityId, "") && BehaviourEnum.validInt(behaviour)) {
                    this.addCustomRule(entityId, BehaviourEnum.fromInt(behaviour));
                }
            }
        } else {
            this.copyCustomRules(defaultConfig);
        }
    }

    /**
     * Creates an {@link NbtCompound} with mod's data. Keys used:
     * <ul><li><b>DefaultBehaviour</b> - <code>int</code> representation of mobs' default behaviour</li>
     * <li><b>AngryTicks</b> - number of ticks after which mobs forgive</li>
     * <li><b>CustomRules</b> - list of following pairs:<ul>
     *     <li><b>EntityIdentifier</b> - <code>string</code> identifier of the entity type (like <code>minecraft:creeper</code>)</li>
     *     <li><b>Behaviour</b> - <code>int</code> representation of entity's custom behaviour</li>
     * </ul></li></ul>
     * @return created compound
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("DefaultBehaviour", this.defaultBehaviour.toInt());
        nbt.putInt("AngryTicks", this.angryTicks);
        NbtList rules = new NbtList();
        NbtCompound rulePair;
        for (String entity : this.customRules.keySet()) {
            rulePair = new NbtCompound();
            rulePair.putString("EntityIdentifier", entity);
            rulePair.putInt("Behaviour", this.entityBehaviour(entity).toInt());
            rules.add(rules.size(), rulePair);
        }
        nbt.put("CustomRules", rules);
        return nbt;
    }

    public static class Builder {
        private final Config config;

        public Builder() {
            this.config = new Config();
        }

        public Builder clearRules() {
            this.config.clearCustomRules();
            return this;
        }

        @Deprecated public Builder defaultRules() {
            this.config.defaultCustomRules();
            return this;
        }

        public Builder addRule(String identifier, BehaviourEnum behaviour) {
            this.config.addCustomRule(identifier, behaviour);
            return this;
        }

        public Builder removeRule(String identifier) {
            this.config.removeCustomRule(identifier);
            return this;
        }

        public Builder setDefaultBehaviour(BehaviourEnum behaviour) {
            this.config.setDefaultBehaviour(behaviour);
            return this;
        }

        public Builder setAngryTicks(int angryTicks) {
            this.config.setAngryTicks(angryTicks);
            return this;
        }

        public Builder setAngrySeconds(int angrySeconds) {
            this.config.setAngrySeconds(angrySeconds);
            return this;
        }

        public Config create() {
            return this.config;
        }
    }
}
