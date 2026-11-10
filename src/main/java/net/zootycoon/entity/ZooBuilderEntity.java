package net.zootycoon.entity;

import net.minecraft.world.level.Level;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.zootycoon.construction.ConstructionManager;
import net.zootycoon.construction.ConstructionSite;
import net.zootycoon.item.ZooItems;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.util.ZooTranslations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ZooBuilderEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> DATA_SKIN_VARIANT =
            SynchedEntityData.defineId(ZooBuilderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_NEEDS_SOMETHING =
            SynchedEntityData.defineId(ZooBuilderEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String[] INDONESIAN_NAMES = {
            "Budi Santoso", "Joko Wibowo", "Agus Setiawan", "Rian Pratama",
            "Dimas Saputra", "Asep Hidayat", "Eko Prasetyo", "Bambang Wijaya",
            "Hendra Gunawan", "Wahyu Nugroho", "Bayu Pratama", "Fajar Ramadhan",
            "Ilham Hakim", "Gilang Kusuma", "Surya Permana", "Dedi Susanto",
            "Iwan Budiman", "Tono Hartono", "Slamet Wicaksono", "Raden Pamungkas",
            "Satria Utama", "Aditya Baskoro", "Andi Wijaya", "Reza Pratama"
    };

    private UUID siteId = null;
    private UUID callTargetUuid = null;
    private int callTicks = 0;

    // Post-construction wage payment state
    private boolean awaitingPayment = false;
    private UUID payerUuid = null;
    private String finishedProjectName = "";
    private int blocksBuilt = 0;
    private int wageDue = 0;
    private int wagePaidSoFar = 0;

    public ZooBuilderEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        if (!level.isClientSide()) {
            int nameIdx = this.random.nextInt(INDONESIAN_NAMES.length);
            String chosenName = INDONESIAN_NAMES[nameIdx];
            this.setCustomName(Component.literal(chosenName));
            this.setCustomNameVisible(true);
            this.setSkinVariant(this.random.nextInt(4));
            this.setItemSlot(EquipmentSlot.HEAD, ZooItems.createHardhat());
            this.setDropChance(EquipmentSlot.HEAD, 0.0f);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.4));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    public boolean isAwaitingPayment() {
        return awaitingPayment;
    }

    public int getWageDue() {
        return wageDue;
    }

    public int getWagePaidSoFar() {
        return wagePaidSoFar;
    }

    public int getRemainingWage() {
        return Math.max(0, wageDue - wagePaidSoFar);
    }

    public void setWagePaidSoFar(int paid) {
        this.wagePaidSoFar = paid;
    }

    public boolean needsSomething() {
        return this.entityData.get(DATA_NEEDS_SOMETHING);
    }

    public void setNeedsSomething(boolean needs) {
        this.entityData.set(DATA_NEEDS_SOMETHING, needs);
    }

    public void updateNeedsIndicator() {
        if (this.level().isClientSide()) return;
        if (this.awaitingPayment && getRemainingWage() > 0) {
            setNeedsSomething(true);
            return;
        }
        ConstructionSite matchedSite = resolveSite();
        if (matchedSite != null && !matchedSite.isCompleted()) {
            if (matchedSite.isClearingPhase()) {
                boolean missingTool = (matchedSite.needsShovel() && !matchedSite.hasShovel())
                        || (matchedSite.needsPickaxe() && !matchedSite.hasPickaxe())
                        || (matchedSite.needsAxe() && !matchedSite.hasAxe());
                setNeedsSomething(missingTool);
            } else {
                setNeedsSomething(!matchedSite.getMissingMaterials().isEmpty());
            }
        } else {
            setNeedsSomething(false);
        }
    }

    private ConstructionSite resolveSite() {
        if (siteId != null) {
            var opt = ConstructionManager.getSite(siteId);
            if (opt.isPresent()) {
                ConstructionSite s = opt.get();
                if (s.getBuilderUuid() == null || !this.getUUID().equals(s.getBuilderUuid())) {
                    s.setBuilderUuid(this.getUUID());
                }
                return s;
            }
        }
        for (var s : ConstructionManager.getActiveSites()) {
            if (this.getUUID().equals(s.getBuilderUuid()) || (siteId != null && siteId.equals(s.getSiteId()))) {
                this.siteId = s.getSiteId();
                if (s.getBuilderUuid() == null || !this.getUUID().equals(s.getBuilderUuid())) {
                    s.setBuilderUuid(this.getUUID());
                }
                return s;
            }
        }
        return null;
    }

    public void setAwaitingPayment(UUID payerUuid, String projectName, int blocksBuilt, int wageDue) {
        this.awaitingPayment = true;
        this.payerUuid = payerUuid;
        this.finishedProjectName = projectName;
        this.blocksBuilt = blocksBuilt;
        this.wageDue = wageDue;
        this.wagePaidSoFar = 0;
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.EMERALD));
        this.setNeedsSomething(true);
    }

    public boolean isOnStrike() {
        // Kuli bangunan dibayar per proyek bangunan, tidak pernah ikut mogok gaji harian
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        if (this.tickCount % 10 == 0) {
            updateNeedsIndicator();
            if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                this.setItemSlot(EquipmentSlot.HEAD, ZooItems.createHardhat());
                this.setDropChance(EquipmentSlot.HEAD, 0.0f);
            }
        }

        if (callTargetUuid != null && this.level() instanceof ServerLevel callLevel) {
            tickCall(callLevel);
        }

        // ==========================================
        // POST-CONSTRUCTION: AWAITING WAGE PAYMENT
        // Kuli follows player and collects Emeralds!
        // ==========================================
        if (this.awaitingPayment) {
            // 1. Follow payer (a pending call takes over navigation)
            if (this.payerUuid != null && !isBeingCalled()) {
                Player payer = this.level().getPlayerByUUID(this.payerUuid);
                if (payer != null && payer.isAlive()) {
                    double distSq = this.distanceToSqr(payer);
                    if (distSq > 2.5 * 2.5) {
                        this.getNavigation().moveTo(payer, 1.25);
                        this.getLookControl().setLookAt(payer, 30.0f, 30.0f);
                    } else {
                        this.getNavigation().stop();
                        this.getLookControl().setLookAt(payer, 30.0f, 30.0f);
                    }
                }
            }

            // 2. Detect & navigate towards / collect dropped Emeralds
            List<net.minecraft.world.entity.item.ItemEntity> nearbyEmeralds = this.level().getEntitiesOfClass(
                    net.minecraft.world.entity.item.ItemEntity.class,
                    this.getBoundingBox().inflate(6.0, 3.0, 6.0),
                    item -> item.isAlive() && item.getItem().is(Items.EMERALD)
            );
            if (!nearbyEmeralds.isEmpty()) {
                var targetEmerald = nearbyEmeralds.get(0);
                double distSq = this.distanceToSqr(targetEmerald);
                if (distSq > 1.8 * 1.8) {
                    this.getNavigation().moveTo(targetEmerald, 1.25);
                    this.getLookControl().setLookAt(targetEmerald, 30.0f, 30.0f);
                } else {
                    ItemStack stack = targetEmerald.getItem();
                    int needed = getRemainingWage();
                    if (needed > 0 && stack.is(Items.EMERALD)) {
                        int take = Math.min(stack.getCount(), needed);
                        stack.shrink(take);
                        this.wagePaidSoFar += take;
                        this.level().playSound(null, this.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.2f);
                        if (stack.isEmpty()) {
                            targetEmerald.discard();
                        }
                        Player payer = this.payerUuid != null ? this.level().getPlayerByUUID(this.payerUuid) : null;
                        if (this.wagePaidSoFar >= this.wageDue) {
                            payoutWage(payer != null ? payer : (Player) this.level().getNearestPlayer(this, 10));
                            return;
                        } else {
                            String bName = this.getCustomName() != null ? this.getCustomName().getString() : ZooTranslations.get("zootycoon.dialog.builder.default_name");
                            if (payer instanceof ServerPlayer sp) {
                                ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.construction", "zootycoon.builder.partial_wage", 2, bName, take, getRemainingWage());
                            }
                        }
                    }
                }
            }

            // DO NOT DISCARD while awaiting payment!
            return;
        }

        // Kuli builder disappears from the world if there is no active project
        if (this.tickCount % 20 == 0 && this.tickCount > 100) {
            if (!ConstructionManager.isLoaded()) {
                return;
            }
            ConstructionSite site = resolveSite();
            boolean hasActiveSite = site != null && !site.isCompleted();
            if (!hasActiveSite) {
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1.0, this.getZ(), 15, 0.3, 0.5, 0.3, 0.05);
                }
                this.discard();
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SKIN_VARIANT, 0);
        builder.define(DATA_NEEDS_SOMETHING, true);
    }

    public int getSkinVariant() {
        return this.entityData.get(DATA_SKIN_VARIANT);
    }

    public void setSkinVariant(int variant) {
        this.entityData.set(DATA_SKIN_VARIANT, Math.max(0, Math.min(3, variant)));
    }

    public UUID getSiteId() {
        return siteId;
    }

    public void setSiteId(UUID siteId) {
        this.siteId = siteId;
        if (siteId != null) {
            this.addTag(siteId.toString());
            this.addTag("zootycoon_builder");
        }
        updateNeedsIndicator();
    }

    public UUID getPayerUuid() {
        return payerUuid;
    }

    public void setPayerUuid(UUID uuid) {
        this.payerUuid = uuid;
    }

    public UUID getOwnerUuid() {
        return payerUuid;
    }

    public void setOwnerUuid(UUID uuid) {
        this.payerUuid = uuid;
    }

    public String getFinishedProjectName() {
        return finishedProjectName != null ? finishedProjectName : "";
    }

    public boolean isBeingCalled() {
        return callTargetUuid != null && callTicks > 0;
    }

    /** True only while the builder still has an unfinished construction site. */
    public boolean hasActiveJob() {
        ConstructionSite site = resolveSite();
        return site != null && !site.isCompleted();
    }

    public void callToPlayer(ServerPlayer player) {
        if (hasActiveJob()) return;
        this.callTargetUuid = player.getUUID();
        this.callTicks = 600;
        if (this.distanceToSqr(player) > 32.0D * 32.0D) {
            this.teleportTo(player.getX() + 1.0, player.getY(), player.getZ() + 1.0);
        } else {
            this.getNavigation().moveTo(player, 1.25D);
        }
    }

    private void finishCall() {
        UUID prevTarget = this.callTargetUuid;
        this.callTicks = 0;
        this.callTargetUuid = null;
        this.getNavigation().stop();
        if (prevTarget != null && this.level() instanceof ServerLevel sLevel) {
            ServerPlayer target = sLevel.getServer().getPlayerList().getPlayer(prevTarget);
            if (target != null) {
                ZooNetworkPayloads.sendAnimalSync(target);
            }
        }
    }

    private void tickCall(ServerLevel sLevel) {
        ServerPlayer target = sLevel.getServer().getPlayerList().getPlayer(callTargetUuid);
        if (target == null || target.level() != sLevel) {
            finishCall();
            return;
        }
        callTicks--;
        double distSq = this.distanceToSqr(target);
        if (distSq <= 9.0D) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            finishCall();
            return;
        }
        // Give up walking when the timer runs out or the path is exhausted far away
        boolean stuck = this.tickCount % 20 == 0 && this.getNavigation().isDone() && callTicks < 560;
        if (callTicks <= 0 || stuck) {
            this.teleportTo(target.getX() + 1.0, target.getY(), target.getZ() + 1.0);
            finishCall();
            return;
        }
        if (this.tickCount % 10 == 0) {
            this.getNavigation().moveTo(target, 1.25D);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;

        if (isOnStrike()) {
            String bName = this.getCustomName() != null ? this.getCustomName().getString() : ZooTranslations.get("zootycoon.dialog.builder.default_name");
            this.level().playSound(null, this.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0f, 1.0f);
            ServerPlayNetworking.send(serverPlayer, new ZooNetworkPayloads.OpenStaffStrikeScreenPayload(
                    this.getId(),
                    this.getUUID(),
                    bName,
                    "zootycoon.dialog.builder.role"
            ));
            return InteractionResult.SUCCESS;
        }

        // 1. Post-construction wage collection
        if (this.awaitingPayment) {
            ItemStack held = player.getMainHandItem();
            int remaining = getRemainingWage();
            if (held.is(Items.EMERALD)) {
                int take = Math.min(held.getCount(), remaining);
                held.shrink(take);
                this.wagePaidSoFar += take;
                this.level().playSound(null, this.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.2f);
                if (this.wagePaidSoFar >= this.wageDue) {
                    payoutWage(player);
                    return InteractionResult.SUCCESS;
                } else {
                    String bName = this.getCustomName() != null ? this.getCustomName().getString() : ZooTranslations.get("zootycoon.dialog.builder.default_name");
                    ZooNetworkPayloads.sendToast(serverPlayer, "zootycoon.toast.title.construction", "zootycoon.builder.partial_wage", 2, bName, take, getRemainingWage());
                    return InteractionResult.SUCCESS;
                }
            }

            openWageDialog(serverPlayer);
            return InteractionResult.SUCCESS;
        }

        ConstructionSite matchedSite = resolveSite();

        if (matchedSite != null) {
            // Open Builder Dialogue Screen
            List<ZooNetworkPayloads.ItemEntry> missingList = new ArrayList<>();
            for (var entry : matchedSite.getMissingMaterials().entrySet()) {
                if (entry.getKey() != null) {
                    ResourceLocation key = BuiltInRegistries.ITEM.getKey(entry.getKey());
                    if (key != null) {
                        missingList.add(new ZooNetworkPayloads.ItemEntry(key.toString(), entry.getValue()));
                    }
                }
            }

            String builderName = this.getCustomName() != null ? this.getCustomName().getString() : ZooTranslations.get("zootycoon.dialog.builder.default_name");
            String schematicName = matchedSite.getSchematic() != null ? matchedSite.getSchematic().getName() : ZooTranslations.get("zootycoon.dialog.builder.default_project");
            int phase = matchedSite.isClearingPhase() ? 0 : 1;
            int obsCount = matchedSite.getObstructingBlocks().size();

            ServerPlayNetworking.send(serverPlayer, new ZooNetworkPayloads.OpenBuilderScreenPayload(
                    matchedSite.getSiteId(),
                    schematicName,
                    matchedSite.getProgressPercentage(),
                    matchedSite.getPlacedBlocksCount(),
                    matchedSite.getTotalBlocks(),
                    missingList,
                    builderName,
                    phase,
                    obsCount,
                    matchedSite.needsShovel(), matchedSite.hasShovel(),
                    matchedSite.needsPickaxe(), matchedSite.hasPickaxe(),
                    matchedSite.needsAxe(), matchedSite.hasAxe(),
                    0,
                    this.getUUID()
            ));
            return InteractionResult.SUCCESS;
        } else {
            String builderName = this.getCustomName() != null ? this.getCustomName().getString() : ZooTranslations.get("zootycoon.dialog.builder.default_name");
            ServerPlayNetworking.send(serverPlayer, new ZooNetworkPayloads.OpenBuilderScreenPayload(
                    UUID.randomUUID(),
                    ZooTranslations.get("zootycoon.dialog.builder.no_project"),
                    0.0f,
                    0,
                    0,
                    List.of(),
                    builderName,
                    3, // Phase 3: Idle
                    0,
                    false, false, false, false, false, false,
                    0,
                    this.getUUID()
            ));
            return InteractionResult.SUCCESS;
        }
    }

    public void tryPayWageFromPlayer(ServerPlayer player) {
        if (!this.awaitingPayment) return;
        int remaining = getRemainingWage();
        if (net.zootycoon.economy.TransactionHelper.hasEnoughEmeralds(player, remaining)) {
            net.zootycoon.economy.TransactionHelper.consumeEmeralds(player, remaining);
            this.wagePaidSoFar = this.wageDue;
            payoutWage(player);
        } else {
            int current = net.zootycoon.economy.TransactionHelper.countEmeralds(player);
            ZooNetworkPayloads.sendToast(player, "zootycoon.toast.title.economy", "zootycoon.builder.not_enough_emerald_wage", 3, remaining, current);
            player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    public void openWageDialog(ServerPlayer player) {
        String bName = this.getCustomName() != null ? this.getCustomName().getString() : ZooTranslations.get("zootycoon.dialog.builder.default_name");
        ServerPlayNetworking.send(player, new ZooNetworkPayloads.OpenBuilderScreenPayload(
                this.siteId != null ? this.siteId : UUID.randomUUID(),
                this.finishedProjectName != null && !this.finishedProjectName.isEmpty() ? this.finishedProjectName : ZooTranslations.get("zootycoon.dialog.builder.default_project"),
                100.0f,
                this.blocksBuilt,
                this.blocksBuilt,
                List.of(),
                bName,
                2, // Phase 2: Gajian
                0,
                false, false, false, false, false, false,
                getRemainingWage(),
                this.getUUID()
        ));
    }

    public void payoutWage(Player player) {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel sl) {
            String builderName = this.getCustomName() != null ? this.getCustomName().getString() : ZooTranslations.get("zootycoon.dialog.builder.default_name");
            sl.playSound(null, this.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.2f);
            sl.playSound(null, this.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1.0f, 1.0f);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2, this.getZ(), 20, 0.4, 0.4, 0.4, 0.05);

            if (player instanceof ServerPlayer sp) {
                ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.success", "zootycoon.builder.wage_paid_farewell", 1, builderName, this.wageDue);
                net.zootycoon.economy.ZooStaffManager.get(sl).syncToPlayer(sp);
            }

            this.setNeedsSomething(false);
            sl.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1.0, this.getZ(), 25, 0.35, 0.5, 0.35, 0.05);
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (siteId != null) {
            tag.putString("SiteId", siteId.toString());
        }
        tag.putInt("SkinVariant", getSkinVariant());
        tag.putBoolean("AwaitingPayment", awaitingPayment);
        if (payerUuid != null) {
            tag.putString("PayerUuid", payerUuid.toString());
        }
        if (finishedProjectName != null) {
            tag.putString("FinishedProjectName", finishedProjectName);
        }
        tag.putInt("BlocksBuilt", blocksBuilt);
        tag.putInt("WageDue", wageDue);
        tag.putInt("WagePaidSoFar", wagePaidSoFar);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        String idStr = tag.getString("SiteId");
        if (!idStr.isEmpty()) {
            try {
                this.siteId = UUID.fromString(idStr);
            } catch (Exception ignored) {}
        }
        setSkinVariant(tag.getInt("SkinVariant"));
        this.awaitingPayment = tag.getBoolean("AwaitingPayment");
        String pStr = tag.getString("PayerUuid");
        if (!pStr.isEmpty()) {
            try {
                this.payerUuid = UUID.fromString(pStr);
            } catch (Exception ignored) {}
        }
        this.finishedProjectName = tag.getString("FinishedProjectName");
        this.blocksBuilt = tag.getInt("BlocksBuilt");
        this.wageDue = tag.getInt("WageDue");
        this.wagePaidSoFar = tag.getInt("WagePaidSoFar");
    }
}
