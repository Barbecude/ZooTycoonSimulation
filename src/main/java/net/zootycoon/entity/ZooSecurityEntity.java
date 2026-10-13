package net.zootycoon.entity;

import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class ZooSecurityEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> SECURITY_NAME =
            SynchedEntityData.defineId(ZooSecurityEntity.class, EntityDataSerializers.STRING);

    private static final String[] NAMES = new String[]{
            "Bambang", "Agus", "Tejo", "Bram", "Dika", "Eko", "Joko", "Sultan", "Wahyu", "Rizky", "Prasetyo", "Budi"
    };

    private UUID ownerUuid = null;

    public ZooSecurityEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setCustomNameVisible(true);
        this.xpReward = 10;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        String randomName = NAMES[(int) (Math.random() * NAMES.length)];
        builder.define(SECURITY_NAME, randomName);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        // Priority 1: Hunt and eliminate Hunter mobs that invade the zoo!
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, ZooHunterEntity.class, true));
        // Priority 2: Retaliate against attackers
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && tickCount % 20 == 0) {
            String name = getSecurityName();
            if (getTarget() instanceof ZooHunterEntity) {
                this.setCustomName(Component.literal(name + " " + net.zootycoon.util.ZooTranslations.get("zootycoon.entity.security.combat_tag")).withStyle(ChatFormatting.RED));
            } else {
                this.setCustomName(Component.literal(name + " " + net.zootycoon.util.ZooTranslations.get("zootycoon.entity.security.tag")).withStyle(ChatFormatting.AQUA));
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer sPlayer) {
            String name = getSecurityName();
            boolean isAlert = getTarget() != null;
            String targetName = isAlert ? getTarget().getName().getString() : "";
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sPlayer, new net.zootycoon.network.ZooNetworkPayloads.OpenSecurityScreenPayload(
                    this.getId(),
                    this.getUUID(),
                    name,
                    isAlert,
                    targetName,
                    (int) this.getHealth(),
                    (int) this.getMaxHealth()
            ));
            this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public void setSecurityName(String name) {
        this.entityData.set(SECURITY_NAME, name);
    }

    public String getSecurityName() {
        return this.entityData.get(SECURITY_NAME);
    }

    public void setOwnerUuid(UUID uuid) {
        this.ownerUuid = uuid;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("SecurityName", getSecurityName());
        if (ownerUuid != null) {
            tag.putString("OwnerUuid", ownerUuid.toString());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        String name = tag.getString("SecurityName");
        if (!name.isEmpty()) {
            setSecurityName(name);
        }
        String oStr = tag.getString("OwnerUuid");
        if (!oStr.isEmpty()) {
            try {
                this.ownerUuid = UUID.fromString(oStr);
            } catch (Exception ignored) {}
        }
    }
}

