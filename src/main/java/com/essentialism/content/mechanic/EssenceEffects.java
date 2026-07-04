package com.essentialism.content.mechanic;

import com.essentialism.Essentialism;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Custom status effects for the Essentialism mod.
 */
public final class EssenceEffects {

    public static final DeferredRegister<MobEffect> REGISTRY =
            DeferredRegister.create(Registries.MOB_EFFECT, Essentialism.MOD_ID);

    /**
     * 本质超载 — increased essence yield at cost of random damage.
     */
    public static final Supplier<MobEffect> ESSENCE_OVERLOAD = REGISTRY.register(
            "essence_overload",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0xFF6A00) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    int interval = Math.max(20, 100 >> amplifier);
                    return duration % interval == 0;
                }

                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
                    float damage = 1.0F + amplifier * 0.5F;
                    entity.hurt(entity.damageSources().magic(), damage);
                    return true;
                }
            }
    );

    /**
     * 灵韵枯萎 — applies weakness periodically.
     */
    public static final Supplier<MobEffect> AURA_WITHER = REGISTRY.register(
            "aura_wither",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x884488) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return duration % 40 == 0;
                }

                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
                    entity.addEffect(new MobEffectInstance(
                            MobEffects.WEAKNESS, 60, amplifier,
                            false, false, true
                    ));
                    return true;
                }
            }
    );

    /**
     * 共鸣过载 — drains hunger rapidly.
     */
    public static final Supplier<MobEffect> RESONANCE_OVERLOAD = REGISTRY.register(
            "resonance_overload",
            () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xFFFF55) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return duration % 10 == 0;
                }

                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
                    if (entity instanceof Player player) {
                        player.causeFoodExhaustion(0.5F + amplifier * 0.25F);
                    }
                    return true;
                }
            }
    );

    /**
     * 时空眩晕 — random teleport + nausea.
     */
    public static final Supplier<MobEffect> SPACETIME_VERTIGO = REGISTRY.register(
            "spacetime_vertigo",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x5555FF) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return duration % 60 == 0;
                }

                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
                    double range = 3.0 + amplifier * 2.0;
                    double dx = (entity.getRandom().nextDouble() - 0.5) * range * 2;
                    double dy = (entity.getRandom().nextDouble() - 0.3) * range;
                    double dz = (entity.getRandom().nextDouble() - 0.5) * range * 2;

                    entity.randomTeleport(
                            entity.getX() + dx,
                            entity.getY() + dy,
                            entity.getZ() + dz,
                            true
                    );

                    level.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.PORTAL,
                            entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                            20, 0.5, 0.5, 0.5, 0.1
                    );
                    return true;
                }
            }
    );

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }

    private EssenceEffects() {}
}
