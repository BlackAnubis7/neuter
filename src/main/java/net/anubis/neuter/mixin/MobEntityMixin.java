package net.anubis.neuter.mixin;

import net.anubis.neuter.Neuter;
import net.anubis.neuter.config.BehaviourEnum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
    private int calmDownAge = -1;
    @Nullable @Shadow private LivingEntity target;

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    private boolean shouldMobCalmDown() {
        return switch (Neuter.getConfig().entityBehaviour(this)) {
            case PASSIVE -> true;
            case NEUTRAL -> age >= calmDownAge;
            case HOSTILE -> false;
        };
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo ci) {
        if(this.getAttacker() instanceof PlayerEntity && Neuter.getConfig().entityBehaviour(this) == BehaviourEnum.NEUTRAL) {
            calmDownAge = this.getLastAttackedTime() + Neuter.getConfig().getAngryTicks();
        }  // else - either always passive or always hostile
    }

    @Inject(at = @At("HEAD"), method = "getTarget")
    public void getTarget(CallbackInfoReturnable<LivingEntity> cir) {
        if (target instanceof PlayerEntity && shouldMobCalmDown()) {
            this.target = null;
            Neuter.LOGGER.info("Mob's not angry at you. Mob is a: " + EntityType.getId(this.getType()));
        }
    }
}
