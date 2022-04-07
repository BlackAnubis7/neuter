package net.anubis.neuter.config;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public enum BehaviourEnum {
    PASSIVE,  // mobs never attack player
    NEUTRAL,  // mobs attack player only when provoked
    HOSTILE;  // mobs behave normally

    public int toInt() {
        return switch (this) {
            case PASSIVE -> 1;
            case NEUTRAL -> 2;
            case HOSTILE -> 3;
        };
    }

    public static BehaviourEnum fromInt(int id) {
        return switch (id) {
            case 1 -> PASSIVE;
            case 2 -> NEUTRAL;
            default -> HOSTILE;
        };
    }

    public MutableText toText() {
        return switch (this) {
            case PASSIVE -> new TranslatableText("command.neuter.behaviour.passive_capital");
            case NEUTRAL -> new TranslatableText("command.neuter.behaviour.neutral_capital");
            case HOSTILE -> new TranslatableText("command.neuter.behaviour.hostile_capital");
        };
    }

    /**
     * Checks if <code>int</code> representation represents a valid <code>BehaviourEnum</code>
     * @param id representation to be checked
     * @return <b>true</b> if the number represents a <code>BehaviourEnum</code>
     */
    public static boolean validInt(int id) {
        return 1 <= id && id <= 3;
    }
}
