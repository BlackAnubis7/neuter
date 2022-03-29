package net.anubis.neuter.config;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void copyConfig() {
        Config a = new Config.Builder().setDefaultBehaviour(BehaviourEnum.PASSIVE)
                .addRule("test:identifier1", BehaviourEnum.HOSTILE).create();
        Config b = new Config.Builder().setDefaultBehaviour(BehaviourEnum.NEUTRAL)
                .addRule("test:identifier2", BehaviourEnum.HOSTILE).create();
        b.copyFrom(a);
        assertEquals(a.getDefaultBehaviour(), b.getDefaultBehaviour());
        assertEquals(a.entityBehaviour("test:identifier1"), b.entityBehaviour("test:identifier1"));
        assertEquals(a.entityBehaviour("test:identifier2"), b.entityBehaviour("test:identifier2"));
    }

    @Test
    void testEntityBehaviourString() {
        String entity1 = "minecraft:creeper";
        String entity2 = "minecraft:spider";
        Config config = new Config.Builder().setDefaultBehaviour(BehaviourEnum.PASSIVE)
                .addRule("minecraft:creeper", BehaviourEnum.HOSTILE).create();
        assertEquals(BehaviourEnum.HOSTILE, config.entityBehaviour(entity1));
        assertEquals(BehaviourEnum.PASSIVE, config.entityBehaviour(entity2));
    }

    @Test
    void testEntityBehaviourIdentifier() {
        Identifier entity1 = new Identifier("minecraft", "creeper");
        Identifier entity2 = new Identifier("minecraft", "spider");
        Config config = new Config.Builder().setDefaultBehaviour(BehaviourEnum.PASSIVE)
                .addRule("minecraft:creeper", BehaviourEnum.HOSTILE).create();
        assertEquals(BehaviourEnum.HOSTILE, config.entityBehaviour(entity1));
        assertEquals(BehaviourEnum.PASSIVE, config.entityBehaviour(entity2));
    }

    @Test
    void fromNbt() {
        NbtCompound nbt = new NbtCompound();
        NbtCompound rulePair1 = new NbtCompound();
        NbtCompound rulePair2 = new NbtCompound();
        NbtCompound rulePair3 = new NbtCompound();
        NbtList rules = new NbtList();
        nbt.putInt("AngryTicks", 1234);
        rulePair1.putString("EntityIdentifier", "minecraft:creeper");
        rulePair1.putInt("Behaviour", BehaviourEnum.HOSTILE.toInt());
        rulePair2.putString("EntityIdentifier", "minecraft:pig");
        rulePair3.putString("EntityIdentifier", "minecraft:spider");
        rulePair3.putInt("Behaviour", BehaviourEnum.HOSTILE.toInt());
        rules.add(0, rulePair1);
        rules.add(0, rulePair2);
        rules.add(0, rulePair3);
        nbt.put("CustomRules", rules);
        nbt.putInt("InvalidKey", 789);

        Config def = new Config(BehaviourEnum.PASSIVE);
        Config conf = new Config.Builder().setDefaultBehaviour(BehaviourEnum.NEUTRAL)
                .addRule("minecraft:spider", BehaviourEnum.NEUTRAL)
                .addRule("minecraft:cow", BehaviourEnum.NEUTRAL)
                .setAngryTicks(555)
                .create();

        conf.fromNbt(nbt, def);

        assertEquals(1234, conf.getAngryTicks());
        assertEquals(def.getDefaultBehaviour(), conf.getDefaultBehaviour());
        assertTrue(conf.getCustomRules().containsKey("minecraft:creeper"));
        assertTrue(conf.getCustomRules().containsKey("minecraft:spider"));
        assertFalse(conf.getCustomRules().containsKey("minecraft:pig"));
        assertFalse(conf.getCustomRules().containsKey("minecraft:cow"));
        assertEquals(BehaviourEnum.HOSTILE, conf.getCustomRules().get("minecraft:spider"));
    }

    @Test
    void toNbt() {
        Config conf = new Config.Builder().setDefaultBehaviour(BehaviourEnum.PASSIVE)
                .setAngryTicks(4321)
                .addRule("minecraft:creeper", BehaviourEnum.HOSTILE)
                .addRule("minecraft:spider", BehaviourEnum.NEUTRAL)
                .create();
        NbtCompound nbt = conf.toNbt();

        assertEquals(BehaviourEnum.PASSIVE.toInt(), nbt.getInt("DefaultBehaviour"));
        assertEquals(4321, nbt.getInt("AngryTicks"));
        assertEquals(2, ((NbtList) Objects.requireNonNull(nbt.get("CustomRules"))).size());
    }

//    NEEDS MINECRAFT RUNTIME VARIABLES - THUS UNABLE TO TEST
//    @Test
//    void testEntityBehaviourEntityType() {
//        EntityType<CreeperEntity> entity1 = EntityType.CREEPER;
//        EntityType<SpiderEntity> entity2 = EntityType.SPIDER;
//        Config config = new Config.Builder().setDefaultBehaviour(BehaviourEnum.PASSIVE)
//                .addRule("minecraft:creeper", BehaviourEnum.HOSTILE).create();
//        assertEquals(BehaviourEnum.HOSTILE, config.entityBehaviour(entity1));
//        assertEquals(BehaviourEnum.PASSIVE, config.entityBehaviour(entity2));
//    }
}