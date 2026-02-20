package com.example.simulation;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import java.util.List;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Zookeeper NPC that specializes in caring for specific animals
 * - Automatically fills feeding stations with appropriate food
 * - Has a specific animal specialization (1 zookeeper per animal)
 * - Operates within activation radius around their specialized animal
 */
public class ZookeeperEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> SPECIALIZATION = 
        SynchedEntityData.defineId(ZookeeperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> ZOOKEEPER_NAME = 
        SynchedEntityData.defineId(ZookeeperEntity.class, EntityDataSerializers.STRING);
    
    private static final int ACTIVATION_RADIUS = 32; // Blocks
    private static final int FEEDING_COOLDOWN = 400; // Ticks (20 seconds)
    private static final int SPECIALIZATION_RADIUS = 10; // minimum requested

    private String assignedAnimalId = ""; // e.g., "alexsmobs:elephant"
    private String assignedFoodId = "";
    private int feedingCooldown = 0;

    public ZookeeperEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.3D));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SPECIALIZATION, 0);
        this.entityData.define(ZOOKEEPER_NAME, "Zookeeper");
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Reduce feeding cooldown
            if (feedingCooldown > 0) {
                feedingCooldown--;
            }

            // Attempt to fill feeding stations
            if (feedingCooldown <= 0 && !assignedAnimalId.isEmpty()) {
                ensureAssignedFood();
                attemptFeedingStation();
                feedingCooldown = FEEDING_COOLDOWN;
            }
        }
    }

    private void attemptFeedingStation() {
        Entity specialized = findSpecializedAnimal();
        if (specialized == null) return;

        // Find nearby animal feeder blocks and fill them with appropriate food
        BlockPos zookeeperPos = this.blockPosition();
        
        for (BlockPos pos : BlockPos.betweenClosed(
                zookeeperPos.offset(-ACTIVATION_RADIUS, -5, -ACTIVATION_RADIUS),
                zookeeperPos.offset(ACTIVATION_RADIUS, 5, ACTIVATION_RADIUS))) {
            
            BlockState state = this.level().getBlockState(pos);
            
            // Check if it's an animal feeder block
            if (state.is(IndoZooTycoon.ANIMAL_FEEDER_BLOCK.get())) {
                BlockEntity blockEntity = this.level().getBlockEntity(pos);
                if (blockEntity instanceof AnimalFeederBlockEntity feeder) {
                    if (specialized.blockPosition().distSqr(pos) > (SPECIALIZATION_RADIUS * SPECIALIZATION_RADIUS)) continue;
                    net.minecraft.resources.ResourceLocation foodId = net.minecraft.resources.ResourceLocation.tryParse(assignedFoodId);
                    Item food = foodId != null ? ForgeRegistries.ITEMS.getValue(foodId) : null;
                    if (food == null) continue;
                    if (!FoodAnimalRegistry.isValidFoodForAnimal(assignedAnimalId, food)) continue;
                    feeder.addFood(new ItemStack(food), 20, assignedAnimalId);
                    return;
                }
            }
        }
    }

    private void ensureAssignedFood() {
        if (!assignedFoodId.isEmpty()) return;
        List<Item> foods = FoodAnimalRegistry.getFoodsForAnimal(assignedAnimalId);
        if (foods.isEmpty()) return;
        net.minecraft.resources.ResourceLocation id = ForgeRegistries.ITEMS.getKey(foods.get(0));
        if (id != null) assignedFoodId = id.toString();
    }

    private Entity findSpecializedAnimal() {
        ZooData data = ZooData.get(this.level());
        net.minecraft.nbt.ListTag tagged = data.getTaggedAnimals();
        for (int i = 0; i < tagged.size(); i++) {
            CompoundTag tag = tagged.getCompound(i);
            if (!assignedAnimalId.equals(tag.getString("type"))) continue;
            Entity e = this.level().getEntity(tag.getInt("id"));
            if (e != null && e.isAlive()) return e;
        }
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("AssignedAnimal", assignedAnimalId);
        tag.putString("AssignedFood", assignedFoodId);
        tag.putString("ZookeeperName", this.entityData.get(ZOOKEEPER_NAME));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.assignedAnimalId = tag.getString("AssignedAnimal");
        this.assignedFoodId = tag.getString("AssignedFood");
        if (tag.contains("ZookeeperName")) {
            this.entityData.set(ZOOKEEPER_NAME, tag.getString("ZookeeperName"));
        }
    }

    public void setAssignedAnimal(String animalId) {
        this.assignedAnimalId = animalId;
        this.assignedFoodId = "";
        ensureAssignedFood();
    }

    public String getAssignedAnimal() {
        return assignedAnimalId;
    }

    public void setZookeeperName(String name) {
        this.entityData.set(ZOOKEEPER_NAME, name);
    }

    public String getZookeeperName() {
        return this.entityData.get(ZOOKEEPER_NAME);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag tag) {
        // Generate random zookeeper name
        String[] names = {"Budi", "Siti", "Ahmad", "Dewi", "Hendra", "Ratna", "Bambang", "Siti", "Anto", "Rina"};
        String randomName = names[this.getRandom().nextInt(names.length)];
        setZookeeperName(randomName);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
    }
}
