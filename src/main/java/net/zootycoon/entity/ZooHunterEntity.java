package net.zootycoon.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ZooHunterEntity extends PathfinderMob {

    public ZooHunterEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.08F);
        this.setCustomName(Component.literal("Pemburu Liar").withStyle(ChatFormatting.RED));
        this.setCustomNameVisible(true);
        this.xpReward = 15;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));

        // Priority 1: Hunter targets zoo animals to hunt/steal them!
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        // Priority 2: Fights back against Security Guards
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, ZooSecurityEntity.class, true));
        // Priority 3: Retaliates if hit
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (level() instanceof ServerLevel sLevel) {
            // Drop bounty rewards: emeralds & arrows
            this.spawnAtLocation(new ItemStack(Items.EMERALD, 1 + random.nextInt(3)));
            this.spawnAtLocation(new ItemStack(Items.ARROW, 2 + random.nextInt(4)));
            sLevel.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0, getZ(), 15, 0.3, 0.5, 0.3, 0.05);
            sLevel.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.0f, 1.0f);

            // Notify nearby players that the threat is neutralized
            for (var p : sLevel.players()) {
                if (p.distanceToSqr(this) < 40 * 40) {
                    p.displayClientMessage(
                            Component.literal("[Keamanan Zoo] Pemburu liar berhasil dilumpuhkan!").withStyle(ChatFormatting.GREEN),
                            true
                    );
                }
            }
        }
    }
}

