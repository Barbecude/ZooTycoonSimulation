package com.example.simulation;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Security NPC that protects animals and prevents hunters from kidnapping them
 * - Only attacks when hunter attempts to kidnap or damage animals
 * - Will NOT attack until threats are detected
 * - Patrols zoo area and responds to threats with combat
 */
public class SecurityEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> SECURITY_NAME = 
        SynchedEntityData.defineId(SecurityEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> ALERT_LEVEL = 
        SynchedEntityData.defineId(SecurityEntity.class, EntityDataSerializers.INT);
    
    private static final int PATROL_RADIUS = 40; // Blocks
    private static final int THREAT_DETECTION_RADIUS = 32; // Blocks
    
    private VisitorEntity hunterId = null;
    private int alertCooldown = 0;
    private boolean huntingMode = false;

    public SecurityEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.xpReward = 15;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SECURITY_NAME, "Security");
        this.entityData.define(ALERT_LEVEL, 0); // 0=calm, 1=alert, 2=hostile
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Reduce alert cooldown
            if (alertCooldown > 0) {
                alertCooldown--;
            } else {
                setAlertLevel(0);
            }

            // Check for hunters attempting to kidnap/attack animals
            if (alertCooldown <= 0) {
                detectThreats();
            }

            // If in hunting mode, maintain aggression
            if (huntingMode && hunterId != null) {
                if (hunterId.isAlive() && this.distanceTo(hunterId) < THREAT_DETECTION_RADIUS) {
                    this.setTarget(hunterId);
                    setAlertLevel(2);
                    alertCooldown = 200; // 10 seconds
                } else {
                    hunterId = null;
                    huntingMode = false;
                    setAlertLevel(0);
                }
            }
        }
    }

    private void detectThreats() {
        double detectionRange = THREAT_DETECTION_RADIUS;

        java.util.List<VisitorEntity> nearbyVisitors = this.level().getEntitiesOfClass(VisitorEntity.class,
                this.getBoundingBox().inflate(detectionRange));

        for (VisitorEntity visitor : nearbyVisitors) {
            if (!visitor.isHunter()) continue;
            if (!visitor.hasActiveHunterCrime()) continue;
            boolean isCarryingAnimal = visitor.isCarrying() || visitor.isEscapeMode();

            if (isCarryingAnimal || visitor.hasActiveHunterCrime()) {
                hunterId = visitor;
                huntingMode = true;
                this.setTarget(visitor);
                setAlertLevel(2);
                alertCooldown = 600; // 30 seconds
                this.playSound(net.minecraft.sounds.SoundEvents.ARROW_HIT, 1.0F, 1.0F);
                return;
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("SecurityName", this.entityData.get(SECURITY_NAME));
        tag.putInt("AlertLevel", this.entityData.get(ALERT_LEVEL));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SecurityName")) {
            this.entityData.set(SECURITY_NAME, tag.getString("SecurityName"));
        }
        if (tag.contains("AlertLevel")) {
            this.entityData.set(ALERT_LEVEL, tag.getInt("AlertLevel"));
        }
    }

    public void setSecurityName(String name) {
        this.entityData.set(SECURITY_NAME, name);
    }

    public String getSecurityName() {
        return this.entityData.get(SECURITY_NAME);
    }

    public void setAlertLevel(int level) {
        this.entityData.set(ALERT_LEVEL, Math.max(0, Math.min(2, level)));
    }

    public int getAlertLevel() {
        return this.entityData.get(ALERT_LEVEL);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData spawnData, CompoundTag tag) {
        // Generate random security name
        String[] names = {"Agus", "Tejo", "Bram", "Dika", "Eko", "Bambang", "Joko", "Ridho", "Sultan", "Wahyu"};
        String randomName = names[this.getRandom().nextInt(names.length)];
        setSecurityName(randomName);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
    }
}
