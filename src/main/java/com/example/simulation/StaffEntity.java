package com.example.simulation;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Staff Entity (Zookeeper) — NPC yang memiliki AI Goals:
 * Prioritas 1: RefillFoodGoal — pergi ke komputer untuk isi ulang makanan
 * Prioritas 2: FeedAnimalGoal — cari hewan & beri makan
 * Prioritas 3: Wander — jalan-jalan santai jika idle
 */
public class StaffEntity extends PathfinderMob {

    /** Jumlah "pakan" yang dibawa staff. 0 = perlu refill. */
    private int foodStock = 0;

    public StaffEntity(EntityType<? extends StaffEntity> type, Level level) {
        super(type, level);
    }

    // ========== Attributes ==========

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    // ========== AI Goals ==========

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RefillFoodGoal(this)); // Cek perut → isi makanan
        this.goalSelector.addGoal(2, new FeedAnimalGoal(this)); // Beri makan hewan
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D)); // Wander
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    // ========== Food Stock Methods ==========

    public int getFoodStock() {
        return foodStock;
    }

    public void setFoodStock(int amount) {
        this.foodStock = amount;
    }

    public boolean hasFood() {
        return foodStock > 0;
    }

    public void consumeOneFood() {
        if (foodStock > 0)
            foodStock--;
    }
}
