package net.zootycoon.entity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zootycoon.animal.ZooAnimalHungerManager;
import net.zootycoon.item.ZooItems;
import net.zootycoon.mixin.LivingEntityAccessor;
import net.zootycoon.network.ZooNetworkPayloads;
import net.zootycoon.sound.ZooSounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ZooVehicleEntity extends Entity {

    private static final EntityDataAccessor<String> DATA_VEHICLE_TYPE = SynchedEntityData.defineId(
            ZooVehicleEntity.class, EntityDataSerializers.STRING
    );
    private static final EntityDataAccessor<Float> DATA_STEER_ANGLE = SynchedEntityData.defineId(
            ZooVehicleEntity.class, EntityDataSerializers.FLOAT
    );
    private static final EntityDataAccessor<Float> DATA_WHEEL_SPIN = SynchedEntityData.defineId(
            ZooVehicleEntity.class, EntityDataSerializers.FLOAT
    );
    private static final EntityDataAccessor<Float> DATA_SPEED = SynchedEntityData.defineId(
            ZooVehicleEntity.class, EntityDataSerializers.FLOAT
    );
    private static final EntityDataAccessor<Boolean> DATA_BRAKING = SynchedEntityData.defineId(
            ZooVehicleEntity.class, EntityDataSerializers.BOOLEAN
    );
    private static final EntityDataAccessor<Boolean> DATA_HEADLIGHTS = SynchedEntityData.defineId(
            ZooVehicleEntity.class, EntityDataSerializers.BOOLEAN
    );

    public record WheelSpec(int cmd, float scale, Vec3 offset, boolean steered) {}

    public record VehicleSpec(
            String typeId,
            int eggCmd,
            float groundClearanceY,
            int bodyCmd,
            float bodyScale,
            Vec3 bodyOffset,
            int steerCmd,
            float steerScale,
            Vec3 steerOffset,
            List<WheelSpec> wheels,
            Vec3 driverSeatOffset,
            boolean hasTrunk,
            int maxCargoAnimals,
            Vec3 trunkPickupOffset,
            List<Vec3> cargoSeatOffsets,
            float maxForwardSpeed,
            float maxReverseSpeed,
            Vec3 headlightOffset
    ) {}

    private static final Map<String, VehicleSpec> SPECS = new LinkedHashMap<>();

    static {
        // 1. Buhanka (Van with rear cargo bay)
        registerSpec(new VehicleSpec(
                "buhanka", 20001, 0.92f,
                10001, 2.0f, new Vec3(0.0, 0.0, 0.0),
                10002, 1.0f, new Vec3(0.469, 0.675, 2.029),
                List.of(
                        new WheelSpec(10003, 1.0f, new Vec3(0.843, -0.468, 1.531), true),
                        new WheelSpec(10004, 1.0f, new Vec3(-0.843, -0.468, 1.531), true),
                        new WheelSpec(10004, 1.0f, new Vec3(-0.843, -0.468, -1.531), false),
                        new WheelSpec(10003, 1.0f, new Vec3(0.843, -0.468, -1.531), false)
                ),
                new Vec3(0.484, 0.45, 1.50),
                true, 2,
                new Vec3(0.0, 0.0, -2.55),
                List.of(new Vec3(0.0, 0.58, -1.10), new Vec3(0.0, 0.58, -2.05)),
                0.58f, 0.28f,
                new Vec3(0.68, -0.12, 2.30)
        ));

        // 2. Jeep CJ-5 1964 (Open-back 4x4 Jeep)
        registerSpec(new VehicleSpec(
                "jeep_cj-5_1964", 20002, 0.40f,
                10010, 2.0f, new Vec3(0.0, 0.563, 0.0),
                10011, 1.0f, new Vec3(0.386, 0.817, 0.101),
                List.of(
                        new WheelSpec(10012, 1.0f, new Vec3(0.668, 0.0, 1.16), true),
                        new WheelSpec(10012, 1.0f, new Vec3(-0.668, 0.0, 1.16), true),
                        new WheelSpec(10012, 1.0f, new Vec3(-0.668, 0.0, -1.16), false),
                        new WheelSpec(10012, 1.0f, new Vec3(0.668, 0.0, -1.16), false)
                ),
                new Vec3(0.375, 0.08, -0.313),
                true, 1,
                new Vec3(0.0, 0.0, -1.90),
                List.of(new Vec3(0.0, 0.48, -1.15)),
                0.62f, 0.30f,
                new Vec3(0.48, 0.35, 1.65)
        ));

        // 3. Ecto-1A (Long station wagon/ambulance with large rear bay)
        registerSpec(new VehicleSpec(
                "ecto-1a", 20003, 0.38f,
                10005, 4.0f, new Vec3(0.0, 0.5, 0.0),
                0, 1.0f, Vec3.ZERO,
                List.of(
                        new WheelSpec(10006, 1.0f, new Vec3(1.213, 0.0, 2.109), true),
                        new WheelSpec(10006, 1.0f, new Vec3(-1.213, 0.0, 2.109), true),
                        new WheelSpec(10006, 1.0f, new Vec3(-1.19, 0.0, -2.109), false),
                        new WheelSpec(10006, 1.0f, new Vec3(1.19, 0.0, -2.109), false)
                ),
                new Vec3(0.50, -0.05, -0.188),
                true, 2,
                new Vec3(0.0, 0.0, -3.65),
                List.of(new Vec3(0.0, 0.38, -1.65), new Vec3(0.0, 0.38, -2.75)),
                0.66f, 0.30f,
                new Vec3(0.78, 0.30, 3.25)
        ));

        // 4. Hammer Monster Truck (Monster pickup truck with rear cargo bed)
        registerSpec(new VehicleSpec(
                "hammer_monster", 20004, 1.03f,
                10007, 4.0f, new Vec3(0.0, 1.5, 0.0),
                10008, 1.0f, new Vec3(0.768, 1.75, 1.0),
                List.of(
                        new WheelSpec(10009, 1.0f, new Vec3(1.65, -0.125, 2.55), true),
                        new WheelSpec(10009, 1.0f, new Vec3(-1.65, -0.125, 2.55), true),
                        new WheelSpec(10009, 1.0f, new Vec3(-1.65, -0.125, -2.55), false),
                        new WheelSpec(10009, 1.0f, new Vec3(1.65, -0.125, -2.55), false)
                ),
                new Vec3(0.75, 1.60, 0.50),
                true, 2,
                new Vec3(0.0, 0.0, -3.20),
                List.of(new Vec3(0.0, 1.75, -1.60), new Vec3(0.0, 1.75, -2.65)),
                0.65f, 0.32f,
                new Vec3(1.10, 0.70, 3.65)
        ));

        // 5. Oldsmobile Cutlass Supreme Coupe (1980)
        registerSpec(new VehicleSpec(
                "oldsmobile_cutlass_supreme_coupe_1980", 20005, 1.17f,
                10013, 2.0f, new Vec3(0.0, 0.0, 0.0),
                10014, 1.0f, new Vec3(0.469, -0.427, 0.555),
                List.of(
                        new WheelSpec(10015, 1.0f, new Vec3(0.766, -0.813, 1.531), true),
                        new WheelSpec(10015, 1.0f, new Vec3(-0.766, -0.813, 1.531), true),
                        new WheelSpec(10015, 1.0f, new Vec3(-0.766, -0.813, -1.531), false),
                        new WheelSpec(10015, 1.0f, new Vec3(0.766, -0.813, -1.531), false)
                ),
                new Vec3(0.45, -0.40, 0.0),
                true, 1,
                new Vec3(0.0, 0.0, -2.65),
                List.of(new Vec3(0.0, 0.50, -1.95)),
                0.68f, 0.30f,
                new Vec3(0.65, -0.45, 2.35)
        ));

        // 6. NEFAZ-66052 Tanker Truck
        registerSpec(new VehicleSpec(
                "tt-66052", 20006, 0.58f,
                10016, 4.0f, new Vec3(0.0, 1.344, 0.0),
                10017, 1.0f, new Vec3(0.812, 1.505, 3.577),
                List.of(
                        new WheelSpec(10018, 1.0f, new Vec3(1.156, 0.0, 2.766), true),
                        new WheelSpec(10019, 1.0f, new Vec3(-1.156, 0.0, 2.766), true),
                        new WheelSpec(10020, 1.0f, new Vec3(-1.031, 0.0, -1.344), false),
                        new WheelSpec(10020, 1.0f, new Vec3(1.031, 0.0, -1.344), false),
                        new WheelSpec(10020, 1.0f, new Vec3(-1.031, 0.0, -2.844), false),
                        new WheelSpec(10020, 1.0f, new Vec3(1.031, 0.0, -2.844), false)
                ),
                new Vec3(0.812, 0.92, 3.00),
                true, 2,
                new Vec3(0.0, 0.0, -4.30),
                List.of(new Vec3(0.0, 1.40, -2.00), new Vec3(0.0, 1.40, -3.30)),
                0.55f, 0.25f,
                new Vec3(0.88, 0.85, 3.85)
        ));

        // 7. Vanna (Small bathtub car - no trunk)
        registerSpec(new VehicleSpec(
                "vanna", 20007, 0.24f,
                10021, 1.0f, new Vec3(0.0, 0.531, 0.0),
                10022, 1.0f, new Vec3(0.0, 0.95, 0.515),
                List.of(
                        new WheelSpec(10023, 1.0f, new Vec3(0.594, 0.0, 0.796), true),
                        new WheelSpec(10023, 1.0f, new Vec3(-0.594, 0.0, 0.796), true),
                        new WheelSpec(10024, 1.0f, new Vec3(0.0, 0.0, -0.796), false)
                ),
                new Vec3(0.0, 0.05, -0.063),
                false, 0,
                Vec3.ZERO,
                List.of(),
                0.55f, 0.25f,
                new Vec3(0.38, 0.20, 1.25)
        ));

        // 8. ZIL-130 (Large cargo flatbed truck)
        registerSpec(new VehicleSpec(
                "zil-130", 20008, 0.55f,
                10025, 4.5f, new Vec3(0.0, 0.351, 0.0),
                10026, 1.0f, new Vec3(0.527, 0.933, 1.176),
                List.of(
                        new WheelSpec(10027, 1.125f, new Vec3(1.019, 0.0, 2.038), true),
                        new WheelSpec(10027, 1.125f, new Vec3(-1.019, 0.0, 2.038), true),
                        new WheelSpec(10028, 1.125f, new Vec3(-1.089, 0.0, -2.038), false),
                        new WheelSpec(10028, 1.125f, new Vec3(1.089, 0.0, -2.038), false)
                ),
                new Vec3(0.492, 0.33, 0.773),
                true, 2,
                new Vec3(0.0, 0.0, -3.70),
                List.of(new Vec3(0.0, 1.15, -1.45), new Vec3(0.0, 1.15, -2.85)),
                0.58f, 0.26f,
                new Vec3(0.85, 0.50, 3.05)
        ));

        // 9. Armored Truck 4x4 (Black)
        registerSpec(new VehicleSpec(
                "armored_truck_4x4_black", 20009, 0.65f,
                10029, 4.0f, new Vec3(0.0, 1.063, 0.0),
                10030, 1.0f, new Vec3(0.696, 1.191, 0.884),
                List.of(
                        new WheelSpec(10031, 1.0f, new Vec3(1.216, 0.0, 2.13), true),
                        new WheelSpec(10032, 1.0f, new Vec3(-1.216, 0.0, 2.13), true),
                        new WheelSpec(10032, 1.0f, new Vec3(-1.216, 0.0, -2.13), false),
                        new WheelSpec(10031, 1.0f, new Vec3(1.216, 0.0, -2.13), false)
                ),
                new Vec3(0.688, 0.68, 0.188),
                true, 2,
                new Vec3(0.0, 0.0, -3.15),
                List.of(new Vec3(0.0, 1.15, -1.40), new Vec3(0.0, 1.15, -2.35)),
                0.62f, 0.28f,
                new Vec3(0.90, 0.55, 3.15)
        ));

        // 10. Armored Truck 4x4 (Gray-Red)
        registerSpec(new VehicleSpec(
                "armored_truck_4x4_gray-red", 20010, 0.65f,
                10033, 4.0f, new Vec3(0.0, 1.063, 0.0),
                10034, 1.0f, new Vec3(0.696, 1.191, 0.884),
                List.of(
                        new WheelSpec(10035, 1.0f, new Vec3(1.216, 0.0, 2.13), true),
                        new WheelSpec(10036, 1.0f, new Vec3(-1.216, 0.0, 2.13), true),
                        new WheelSpec(10036, 1.0f, new Vec3(-1.216, 0.0, -2.13), false),
                        new WheelSpec(10035, 1.0f, new Vec3(1.216, 0.0, -2.13), false)
                ),
                new Vec3(0.688, 0.68, 0.188),
                true, 2,
                new Vec3(0.0, 0.0, -3.15),
                List.of(new Vec3(0.0, 1.15, -1.40), new Vec3(0.0, 1.15, -2.35)),
                0.62f, 0.28f,
                new Vec3(0.90, 0.55, 3.15)
        ));

        // 11-17. ZAZ-968A & ZAZ-968M variants
        registerZazSpec("zaz-968a_race_01", 20011, 10037, 10038, 10039);
        registerZazSpec("zaz-968a_race_02", 20012, 10040, 10041, 10042);
        registerZazSpec("zaz-968a_tint", 20013, 10043, 10044, 10045);
        registerZazSpec("zaz-968m_futuristic", 20014, 10046, 10047, 10048);
        registerZazSpec("zaz-968m_race_01", 20015, 10049, 10050, 10051);
        registerZazSpec("zaz-968m_race_02", 20016, 10052, 10053, 10054);
        registerZazSpec("zaz-968m_tint", 20017, 10055, 10056, 10057);

        // 18. ZAZ-968T (Tracked variant)
        registerSpec(new VehicleSpec(
                "zaz-968t", 20018, 0.32f,
                10058, 2.0f, new Vec3(0.0, 0.562, 0.0),
                0, 1.0f, Vec3.ZERO,
                List.of(
                        new WheelSpec(10059, 1.0f, new Vec3(0.969, 0.0, 1.5), false),
                        new WheelSpec(10059, 1.0f, new Vec3(-0.969, 0.0, 1.5), false),
                        new WheelSpec(10059, 1.0f, new Vec3(0.969, 0.0, 0.5), false),
                        new WheelSpec(10059, 1.0f, new Vec3(-0.969, 0.0, 0.5), false),
                        new WheelSpec(10059, 1.0f, new Vec3(0.969, 0.0, -0.5), false),
                        new WheelSpec(10059, 1.0f, new Vec3(-0.969, 0.0, -0.5), false),
                        new WheelSpec(10059, 1.0f, new Vec3(0.969, 0.0, -1.5), false),
                        new WheelSpec(10059, 1.0f, new Vec3(-0.969, 0.0, -1.5), false)
                ),
                new Vec3(0.438, -0.22, 0.05),
                true, 1,
                new Vec3(0.0, 0.0, -2.05),
                List.of(new Vec3(0.0, 0.55, -1.10)),
                0.55f, 0.28f,
                new Vec3(0.65, 0.25, 2.05)
        ));
    }

    private static void registerZazSpec(String typeId, int eggCmd, int bodyCmd, int steerCmd, int wheelCmd) {
        registerSpec(new VehicleSpec(
                typeId, eggCmd, 1.00f,
                bodyCmd, 2.0f, new Vec3(0.0, 0.125, 0.0),
                steerCmd, 2.0f, new Vec3(0.438, -0.187, 0.526),
                List.of(
                        new WheelSpec(wheelCmd, 1.0f, new Vec3(0.718, -0.687, 1.219), true),
                        new WheelSpec(wheelCmd, 1.0f, new Vec3(-0.718, -0.687, 1.219), true),
                        new WheelSpec(wheelCmd, 1.0f, new Vec3(-0.718, -0.687, -1.219), false),
                        new WheelSpec(wheelCmd, 1.0f, new Vec3(0.718, -0.687, -1.219), false)
                ),
                new Vec3(0.438, -0.34, 0.05),
                true, 1,
                new Vec3(0.0, 0.0, -2.05),
                List.of(new Vec3(0.0, 0.50, -1.10)),
                0.64f, 0.30f,
                new Vec3(0.52, -0.35, 1.85)
        ));
    }

    private static void registerSpec(VehicleSpec spec) {
        SPECS.put(spec.typeId(), spec);
    }

    public static VehicleSpec getSpec(String typeId) {
        return SPECS.getOrDefault(typeId, SPECS.get("jeep_cj-5_1964"));
    }

    public static String resolveVehicleTypeFromEgg(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(Items.DOLPHIN_SPAWN_EGG)) {
            return null;
        }
        CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (cmd != null) {
            int val = cmd.value();
            if (val == 20019) {
                return "remover";
            }
            for (VehicleSpec spec : SPECS.values()) {
                if (spec.eggCmd() == val) {
                    return spec.typeId();
                }
            }
        }
        CustomData customData = stack.get(DataComponents.ENTITY_DATA);
        if (customData != null && customData.contains("Tags")) {
            CompoundTag nbt = customData.copyTag();
            ListTag tags = nbt.getList("Tags", Tag.TAG_STRING);
            for (int i = 0; i < tags.size(); i++) {
                String t = tags.getString(i);
                if ("phys_veh.remover".equals(t)) {
                    return "remover";
                }
                if (t.startsWith("phys_veh.") && !"phys_veh.spawn".equals(t)) {
                    String shortId = t.substring("phys_veh.".length());
                    if (SPECS.containsKey(shortId)) {
                        return shortId;
                    }
                    if ("armored_truck_4x4".equals(shortId)) {
                        return "armored_truck_4x4_black";
                    }
                    if ("zaz-968a".equals(shortId)) {
                        return "zaz-968a_tint";
                    }
                    if ("zaz-968m".equals(shortId)) {
                        return "zaz-968m_tint";
                    }
                }
            }
        }
        return null;
    }

    private float currentSpeed = 0.0f;
    private float localSteerAngle = 0.0f;
    private float localWheelSpin = 0.0f;
    private boolean localBraking = false;
    private boolean hadDriver = false;
    private boolean wasBraking = false;
    private int engineSoundTimer = 0;
    private int brakeSoundTimer = 0;
    private int crashSoundCooldown = 0;
    private float prevTickSpeed = 0.0f;
    private int lastGearStep = 0;
    private int lerpSteps = 0;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;
    private final Map<UUID, Integer> dismountCooldowns = new HashMap<>();

    public ZooVehicleEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    @Override
    public float maxUpStep() {
        return 1.0F;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_VEHICLE_TYPE, "jeep_cj-5_1964");
        builder.define(DATA_STEER_ANGLE, 0.0f);
        builder.define(DATA_WHEEL_SPIN, 0.0f);
        builder.define(DATA_SPEED, 0.0f);
        builder.define(DATA_BRAKING, false);
        builder.define(DATA_HEADLIGHTS, false);
    }

    public boolean hasHeadlightsOn() {
        boolean isNightOrDark = this.level().isNight() || this.level().getRawBrightness(this.blockPosition(), 0) < 7;
        return this.entityData.get(DATA_HEADLIGHTS) || this.getDriver() != null || isNightOrDark;
    }

    public String getVehicleType() {
        return this.entityData.get(DATA_VEHICLE_TYPE);
    }

    public void setVehicleType(String typeId) {
        if (typeId != null && SPECS.containsKey(typeId)) {
            this.entityData.set(DATA_VEHICLE_TYPE, typeId);
        }
    }

    public VehicleSpec getSpec() {
        return getSpec(getVehicleType());
    }

    public float getSteerAngle() {
        return this.level().isClientSide() && isControlledByLocalInstance()
                ? this.localSteerAngle
                : this.entityData.get(DATA_STEER_ANGLE);
    }

    public float getWheelSpin() {
        return this.level().isClientSide() && isControlledByLocalInstance()
                ? this.localWheelSpin
                : this.entityData.get(DATA_WHEEL_SPIN);
    }

    public Player getDriver() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    public List<Entity> getCargoPassengers() {
        List<Entity> list = new ArrayList<>();
        for (Entity passenger : this.getPassengers()) {
            if (!(passenger instanceof Player)) {
                list.add(passenger);
            }
        }
        return list;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return getDriver();
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        if (passenger instanceof Player) {
            return getDriver() == null;
        }
        VehicleSpec spec = getSpec();
        return spec.hasTrunk() && getCargoPassengers().size() < spec.maxCargoAnimals();
    }

    @Override
    public boolean dismountsUnderwater() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.isAlive();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpXRot = xRot;
        this.lerpSteps = 5;
    }

    private void tickLerp() {
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
            return;
        }
        if (this.lerpSteps > 0) {
            this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
            this.lerpSteps--;
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.tickLerp();

        VehicleSpec spec = getSpec();
        Player driver = getDriver();

        if (this.isControlledByLocalInstance()) {
            if (driver != null) {
                float forwardInput = driver.zza;
                float steerInput = driver.xxa;
                boolean braking = ((LivingEntityAccessor) driver).isJumping();

                // Update steering wheel / front wheel angle (-32 to +32 deg)
                if (steerInput > 0.05f) {
                    this.localSteerAngle = Mth.clamp(this.localSteerAngle + 4.5f, -32.0f, 32.0f);
                } else if (steerInput < -0.05f) {
                    this.localSteerAngle = Mth.clamp(this.localSteerAngle - 4.5f, -32.0f, 32.0f);
                } else {
                    this.localSteerAngle *= 0.72f;
                    if (Math.abs(this.localSteerAngle) < 0.25f) {
                        this.localSteerAngle = 0.0f;
                    }
                }

                // Update forward / reverse speed
                boolean reverseBraking = (forwardInput > 0.05f && this.currentSpeed < -0.04f)
                        || (forwardInput < -0.05f && this.currentSpeed > 0.04f);
                boolean driftBraking = Math.abs(this.currentSpeed) > 0.34f && Math.abs(this.localSteerAngle) > 22.0f;
                this.localBraking = (braking && Math.abs(this.currentSpeed) > 0.025f) || reverseBraking || driftBraking;

                if (braking) {
                    this.currentSpeed *= 0.60f;
                    if (Math.abs(this.currentSpeed) < 0.01f) {
                        this.currentSpeed = 0.0f;
                    }
                } else if (forwardInput > 0.05f) {
                    if (this.currentSpeed < 0.0f) {
                        this.currentSpeed = Math.min(0.0f, this.currentSpeed + 0.06f);
                    } else {
                        this.currentSpeed = Math.min(spec.maxForwardSpeed(), this.currentSpeed + 0.038f);
                    }
                } else if (forwardInput < -0.05f) {
                    if (this.currentSpeed > 0.0f) {
                        this.currentSpeed = Math.max(0.0f, this.currentSpeed - 0.06f);
                    } else {
                        this.currentSpeed = Math.max(-spec.maxReverseSpeed(), this.currentSpeed - 0.026f);
                    }
                } else {
                    this.currentSpeed *= 0.86f;
                    if (Math.abs(this.currentSpeed) < 0.005f) {
                        this.currentSpeed = 0.0f;
                    }
                }

                // Apply turning yaw
                if (Math.abs(this.localSteerAngle) > 0.5f) {
                    if (Math.abs(this.currentSpeed) > 0.006f || Math.abs(forwardInput) > 0.05f) {
                        float reverseDir = (this.currentSpeed < -0.005f || (Math.abs(this.currentSpeed) <= 0.005f && forwardInput < -0.05f))
                                ? -1.0f : 1.0f;
                        float speedFactor = Mth.clamp(Math.abs(this.currentSpeed) / 0.25f, 0.40f, 1.35f);
                        float yawDelta = -(this.localSteerAngle / 32.0f) * 4.4f * speedFactor * reverseDir;
                        this.setYRot(this.getYRot() + yawDelta);
                    } else if (Math.abs(steerInput) > 0.05f) {
                        float yawDelta = -(this.localSteerAngle / 32.0f) * 1.4f;
                        this.setYRot(this.getYRot() + yawDelta);
                    }
                }
            } else {
                this.localBraking = false;
                this.localSteerAngle *= 0.75f;
                this.currentSpeed *= 0.80f;
                if (Math.abs(this.currentSpeed) < 0.005f) {
                    this.currentSpeed = 0.0f;
                }
            }

            double rad = Math.toRadians(this.getYRot());
            double vx = -Math.sin(rad) * this.currentSpeed;
            double vz = Math.cos(rad) * this.currentSpeed;
            double vy = this.isNoGravity() ? 0.0 : (this.onGround() ? -0.08 : Math.max(this.getDeltaMovement().y - 0.08, -1.2));

            this.setDeltaMovement(vx, vy, vz);
            this.move(MoverType.SELF, this.getDeltaMovement());

            if (this.horizontalCollision) {
                this.currentSpeed *= 0.45f;
            }

            this.localWheelSpin = (this.localWheelSpin + this.currentSpeed * 95.0f) % 360.0f;
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }

        // Server-side visual sync + automatic animal trunk boarding
        if (!this.level().isClientSide()) {
            if (driver != null) {
                float sInput = driver.xxa;
                float fInput = driver.zza;
                boolean sJump = ((LivingEntityAccessor) driver).isJumping();
                float sAngle = this.entityData.get(DATA_STEER_ANGLE);
                if (sInput > 0.05f) {
                    sAngle = Mth.clamp(sAngle + 4.5f, -32.0f, 32.0f);
                } else if (sInput < -0.05f) {
                    sAngle = Mth.clamp(sAngle - 4.5f, -32.0f, 32.0f);
                } else {
                    sAngle *= 0.72f;
                }
                this.entityData.set(DATA_STEER_ANGLE, sAngle);

                double dx = this.getX() - this.xo;
                double dz = this.getZ() - this.zo;
                double rad = Math.toRadians(this.getYRot());
                float signedMove = (float) (dx * (-Math.sin(rad)) + dz * Math.cos(rad));
                this.entityData.set(DATA_SPEED, signedMove);
                float spin = (this.entityData.get(DATA_WHEEL_SPIN) + signedMove * 95.0f) % 360.0f;
                this.entityData.set(DATA_WHEEL_SPIN, spin);

                boolean revBrake = (fInput > 0.05f && signedMove < -0.04f) || (fInput < -0.05f && signedMove > 0.04f);
                boolean driftBrake = Math.abs(signedMove) > 0.34f && Math.abs(sAngle) > 22.0f;
                this.entityData.set(DATA_BRAKING, (sJump && Math.abs(signedMove) > 0.025f) || revBrake || driftBrake);
                this.entityData.set(DATA_HEADLIGHTS, true);
            } else {
                this.entityData.set(DATA_STEER_ANGLE, this.localSteerAngle);
                this.entityData.set(DATA_SPEED, this.currentSpeed);
                this.entityData.set(DATA_WHEEL_SPIN, this.localWheelSpin);
                this.entityData.set(DATA_BRAKING, false);
                boolean isNightOrDark = this.level().isNight() || this.level().getRawBrightness(this.blockPosition(), 0) < 7;
                this.entityData.set(DATA_HEADLIGHTS, isNightOrDark);
            }
        } else {
            if (!this.isControlledByLocalInstance()) {
                this.localSteerAngle = this.entityData.get(DATA_STEER_ANGLE);
                this.localWheelSpin = this.entityData.get(DATA_WHEEL_SPIN);
                this.localBraking = this.entityData.get(DATA_BRAKING);
            }
            tickClientEffects(spec, driver);
        }
    }

    private static boolean isHeavyTruck(String typeId) {
        return "tt-66052".equals(typeId)
                || "zil-130".equals(typeId)
                || "hammer_monster".equals(typeId)
                || "armored_truck_4x4_black".equals(typeId)
                || "armored_truck_4x4_gray-red".equals(typeId);
    }

    private static SoundEvent getEngineSoundEvent(String typeId) {
        if (typeId != null && typeId.startsWith("zaz-")) {
            return ZooSounds.VEH_ZAZ_ENGINE;
        }
        if (isHeavyTruck(typeId)) {
            return ZooSounds.VEH_ENGINE_1;
        }
        return ZooSounds.VEH_ENGINE_0;
    }

    private static Vec3 getExhaustLocalOffset(VehicleSpec spec) {
        double minZ = -1.5;
        double minY = 0.0;
        for (WheelSpec wheel : spec.wheels()) {
            if (wheel.offset().z < minZ) {
                minZ = wheel.offset().z;
            }
            if (wheel.offset().y < minY) {
                minY = wheel.offset().y;
            }
        }
        return new Vec3(-0.55, minY + spec.groundClearanceY() + 0.12, minZ - 0.55);
    }

    private void tickClientEffects(VehicleSpec spec, Player driver) {
        String typeId = getVehicleType();
        boolean heavyTruck = isHeavyTruck(typeId);
        float speed = this.isControlledByLocalInstance() ? this.currentSpeed : this.entityData.get(DATA_SPEED);
        float absSpeed = Math.abs(speed);
        boolean activeBraking = this.isControlledByLocalInstance() ? this.localBraking : this.entityData.get(DATA_BRAKING);

        if (this.crashSoundCooldown > 0) {
            this.crashSoundCooldown--;
        }

        // 1. Ignition start / stop sounds & startup exhaust puff
        boolean hasDriverNow = driver != null;
        if (hasDriverNow && !this.hadDriver) {
            this.level().playLocalSound(
                    this.getX(), this.getY() + 0.6, this.getZ(),
                    ZooSounds.VEH_KEY_INSERT, SoundSource.AMBIENT, 0.7f, 1.0f, false
            );
            SoundEvent startSound = (typeId != null && typeId.startsWith("zaz-"))
                    ? ZooSounds.VEH_ZAZ_IGNITION_START
                    : ZooSounds.VEH_IGNITION_START;
            this.level().playLocalSound(
                    this.getX(), this.getY() + 0.6, this.getZ(),
                    startSound, SoundSource.AMBIENT, 0.85f, 1.0f, false
            );

            Vec3 exhaustPos = this.position().add(rotateLocalOffset(getExhaustLocalOffset(spec), this.getYRot()));
            double rad = Math.toRadians(this.getYRot());
            double backX = Math.sin(rad) * 0.04;
            double backZ = -Math.cos(rad) * 0.04;
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(
                        ParticleTypes.SMOKE,
                        exhaustPos.x + (this.random.nextDouble() - 0.5) * 0.1,
                        exhaustPos.y + (this.random.nextDouble() - 0.5) * 0.08,
                        exhaustPos.z + (this.random.nextDouble() - 0.5) * 0.1,
                        backX, 0.02, backZ
                );
            }
            this.engineSoundTimer = 6;
        } else if (!hasDriverNow && this.hadDriver) {
            this.level().playLocalSound(
                    this.getX(), this.getY() + 0.6, this.getZ(),
                    ZooSounds.VEH_KEY_REMOVE, SoundSource.AMBIENT, 0.7f, 1.0f, false
            );
            this.level().playLocalSound(
                    this.getX(), this.getY() + 0.6, this.getZ(),
                    heavyTruck ? ZooSounds.VEH_TRUCK_HANDBRAKE_ON : ZooSounds.VEH_HANDBRAKE_ON,
                    SoundSource.AMBIENT, 0.65f, 1.0f, false
            );
        }
        this.hadDriver = hasDriverNow;

        // 2. Engine loop sound, gear shift sound & subtle exhaust smoke while engine is running
        if (hasDriverNow) {
            float throttle = Math.abs(driver.zza);
            float maxSpd = Math.max(0.25f, spec.maxForwardSpeed());
            float speedRatio = Mth.clamp(absSpeed / maxSpd, 0.0f, 1.0f);

            int gearStep = speed < -0.02f ? -1 : (absSpeed < 0.02f ? 0 : (speedRatio < 0.40f ? 1 : (speedRatio < 0.75f ? 2 : 3)));
            if (gearStep != this.lastGearStep && gearStep != 0) {
                this.level().playLocalSound(
                        this.getX(), this.getY() + 0.5, this.getZ(),
                        ZooSounds.VEH_GEAR_FINISH, SoundSource.AMBIENT, 0.35f, 0.95f + this.random.nextFloat() * 0.1f, false
                );
            }
            this.lastGearStep = gearStep;

            if (this.engineSoundTimer > 0) {
                this.engineSoundTimer--;
            }
            if (this.engineSoundTimer <= 0) {
                float pitch = 0.56f + speedRatio * 0.78f + (throttle > 0.05f ? 0.08f : 0.0f);
                float volume = 0.32f + speedRatio * 0.38f;
                this.level().playLocalSound(
                        this.getX(), this.getY() + 0.6, this.getZ(),
                        getEngineSoundEvent(typeId), SoundSource.AMBIENT, volume, pitch, false
                );
                this.engineSoundTimer = (absSpeed > 0.05f || throttle > 0.05f) ? 2 : 3;
            }

            // Tire rolling sound at moderate/high speed
            if (absSpeed > 0.16f && !activeBraking && this.tickCount % 5 == 0) {
                float tirePitch = 0.75f + speedRatio * 0.45f;
                this.level().playLocalSound(
                        this.getX(), this.getY() + 0.15, this.getZ(),
                        ZooSounds.VEH_TIRES_ROUGH, SoundSource.AMBIENT, 0.22f + speedRatio * 0.18f, tirePitch, false
                );
            }

            // Subtle exhaust smoke ("sedikit asap knalpot")
            int exhaustInterval = (absSpeed > 0.02f || throttle > 0.05f) ? 3 : 7;
            if (this.tickCount % exhaustInterval == 0) {
                Vec3 exhaustPos = this.position().add(rotateLocalOffset(getExhaustLocalOffset(spec), this.getYRot()));
                double rad = Math.toRadians(this.getYRot());
                double backSpeed = 0.025 + speedRatio * 0.035;
                double backX = Math.sin(rad) * backSpeed + (this.random.nextDouble() - 0.5) * 0.01;
                double backZ = -Math.cos(rad) * backSpeed + (this.random.nextDouble() - 0.5) * 0.01;
                this.level().addParticle(
                        ParticleTypes.SMOKE,
                        exhaustPos.x,
                        exhaustPos.y,
                        exhaustPos.z,
                        backX, 0.012 + this.random.nextDouble() * 0.008, backZ
                );
            }
        }

        // 3. Brake sound & tire smoke ("asap ketika rem")
        if (activeBraking && absSpeed > 0.025f) {
            if (!this.wasBraking) {
                this.level().playLocalSound(
                        this.getX(), this.getY() + 0.4, this.getZ(),
                        heavyTruck ? ZooSounds.VEH_TRUCK_HANDBRAKE_ON : ZooSounds.VEH_HANDBRAKE_ON,
                        SoundSource.AMBIENT, 0.55f, 1.0f, false
                );
            }
            if (this.brakeSoundTimer > 0) {
                this.brakeSoundTimer--;
            }
            if (this.brakeSoundTimer <= 0) {
                float skidPitch = 0.90f + this.random.nextFloat() * 0.25f;
                float skidVol = Mth.clamp(0.35f + absSpeed * 0.85f, 0.35f, 0.78f);
                this.level().playLocalSound(
                        this.getX(), this.getY() + 0.15, this.getZ(),
                        ZooSounds.VEH_TIRES_HARD, SoundSource.AMBIENT, skidVol, skidPitch, false
                );
                this.brakeSoundTimer = 3;
            }

            double rad = Math.toRadians(this.getYRot());
            double driftBackX = Math.sin(rad) * 0.02;
            double driftBackZ = -Math.cos(rad) * 0.02;

            for (WheelSpec wheel : spec.wheels()) {
                Vec3 wheelWorld = this.position().add(rotateLocalOffset(
                        new Vec3(wheel.offset().x, 0.0, wheel.offset().z),
                        this.getYRot()
                ));
                double wx = wheelWorld.x + (this.random.nextDouble() - 0.5) * 0.22;
                double wy = this.getY() + 0.10;
                double wz = wheelWorld.z + (this.random.nextDouble() - 0.5) * 0.22;

                if (this.tickCount % 2 == 0 || !wheel.steered()) {
                    this.level().addParticle(
                            ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            wx, wy, wz,
                            driftBackX + (this.random.nextDouble() - 0.5) * 0.015,
                            0.018 + this.random.nextDouble() * 0.012,
                            driftBackZ + (this.random.nextDouble() - 0.5) * 0.015
                    );
                }
                this.level().addParticle(
                        ParticleTypes.CLOUD,
                        wx, wy + 0.04, wz,
                        driftBackX + (this.random.nextDouble() - 0.5) * 0.02,
                        0.015,
                        driftBackZ + (this.random.nextDouble() - 0.5) * 0.02
                );
            }
        }
        this.wasBraking = activeBraking;

        // 4. Crash sound on hard horizontal collision
        if (this.horizontalCollision && Math.abs(this.prevTickSpeed) > 0.18f && this.crashSoundCooldown == 0) {
            this.level().playLocalSound(
                    this.getX(), this.getY() + 0.5, this.getZ(),
                    ZooSounds.VEH_CRASH, SoundSource.AMBIENT, 0.80f, 0.90f + this.random.nextFloat() * 0.2f, false
            );
            this.crashSoundCooldown = 15;
        }
        this.prevTickSpeed = speed;
    }

    public boolean tryLoadNearbyAnimal(Player player) {
        VehicleSpec spec = getSpec();
        if (!spec.hasTrunk()) {
            if (player instanceof ServerPlayer sp) {
                ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.warning", "zootycoon.vehicle.no_trunk", 2);
            }
            return false;
        }
        if (getCargoPassengers().size() >= spec.maxCargoAnimals()) {
            if (player instanceof ServerPlayer sp) {
                ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.warning", "zootycoon.vehicle.trunk_full", 2);
            }
            return false;
        }

        Vec3 trunkWorldPos = this.position().add(rotateLocalOffset(spec.trunkPickupOffset(), this.getYRot()));
        AABB searchBox = AABB.ofSize(trunkWorldPos.add(0.0, 0.9, 0.0), 3.8, 3.0, 3.8);

        List<Mob> nearbyMobs = this.level().getEntitiesOfClass(Mob.class, searchBox, mob -> {
            if (!mob.isAlive() || mob.isPassenger()) {
                return false;
            }
            if (mob instanceof ZooBuilderEntity
                    || mob instanceof ZooZookeeperEntity
                    || mob instanceof ZooSecurityEntity
                    || mob instanceof ZooCashierEntity
                    || mob instanceof ZooVisitorEntity
                    || mob instanceof ZooHunterEntity) {
                return false;
            }
            Integer cooldownUntil = dismountCooldowns.get(mob.getUUID());
            if (cooldownUntil != null && this.tickCount < cooldownUntil) {
                return false;
            }
            return mob instanceof Animal
                    || mob instanceof AmbientCreature
                    || mob instanceof WaterAnimal
                    || ZooAnimalHungerManager.isZooAnimalCandidate(mob);
        });

        if (nearbyMobs.isEmpty()) {
            if (player instanceof ServerPlayer sp) {
                ZooNetworkPayloads.sendToast(sp, "zootycoon.toast.title.info", "zootycoon.vehicle.no_animal_near_trunk", 1);
            }
            return false;
        }

        boolean loadedAny = false;
        for (Mob animal : nearbyMobs) {
            if (getCargoPassengers().size() >= spec.maxCargoAnimals()) {
                break;
            }
            if (animal.isLeashed()) {
                animal.dropLeash(true, true);
            }
            animal.getNavigation().stop();
            if (animal.startRiding(this, true)) {
                loadedAny = true;
                this.level().playSound(null, this.blockPosition(), SoundEvents.HORSE_SADDLE, SoundSource.NEUTRAL, 1.0f, 1.0f);
                if (player instanceof ServerPlayer sp) {
                    ZooNetworkPayloads.sendToast(
                            sp,
                            "zootycoon.toast.title.info",
                            "zootycoon.vehicle.animal_loaded",
                            0,
                            animal.getDisplayName().getString()
                    );
                }
            }
        }
        return loadedAny;
    }

    public boolean unloadCargoAnimals(Player actor) {
        List<Entity> cargo = getCargoPassengers();
        if (cargo.isEmpty()) {
            return false;
        }
        VehicleSpec spec = getSpec();
        Vec3 unloadBase = this.position().add(rotateLocalOffset(
                spec.hasTrunk() ? spec.trunkPickupOffset().add(0.0, 0.25, -0.85) : new Vec3(0.0, 0.25, -2.2),
                this.getYRot()
        ));

        for (int i = 0; i < cargo.size(); i++) {
            Entity passenger = cargo.get(i);
            dismountCooldowns.put(passenger.getUUID(), this.tickCount + 120);
            passenger.stopRiding();
            double spreadX = (i % 2 == 0 ? 0.35 : -0.35) * (i > 0 ? 1.0 : 0.0);
            passenger.teleportTo(unloadBase.x + spreadX, unloadBase.y, unloadBase.z);
        }

        this.level().playSound(null, this.blockPosition(), SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        if (actor instanceof ServerPlayer sp) {
            ZooNetworkPayloads.sendToast(
                    sp,
                    "zootycoon.toast.title.info",
                    "zootycoon.vehicle.animal_unloaded",
                    0
            );
        }
        return true;
    }

    public static Vec3 rotateLocalOffset(Vec3 local, float yawDeg) {
        return local.yRot(-yawDeg * Mth.DEG_TO_RAD);
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        VehicleSpec spec = getSpec();
        if (passenger instanceof Player) {
            Vec3 localSeat = spec.driverSeatOffset();
            Vec3 rotated = rotateLocalOffset(new Vec3(localSeat.x, 0.0, localSeat.z), this.getYRot());
            callback.accept(
                    passenger,
                    this.getX() + rotated.x,
                    this.getY() + localSeat.y,
                    this.getZ() + rotated.z
            );
        } else {
            List<Entity> cargo = getCargoPassengers();
            int idx = Math.max(0, cargo.indexOf(passenger));
            List<Vec3> offsets = spec.cargoSeatOffsets();
            Vec3 localCargo = offsets.isEmpty()
                    ? new Vec3(0.0, 0.6, -1.5)
                    : offsets.get(Math.min(idx, offsets.size() - 1));
            Vec3 rotated = rotateLocalOffset(new Vec3(localCargo.x, 0.0, localCargo.z), this.getYRot());
            callback.accept(
                    passenger,
                    this.getX() + rotated.x,
                    this.getY() + localCargo.y,
                    this.getZ() + rotated.z
            );
            passenger.setYRot(this.getYRot());
            passenger.setYHeadRot(this.getYRot());
            if (passenger instanceof LivingEntity living) {
                living.setYBodyRot(this.getYRot());
            }
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        VehicleSpec spec = getSpec();
        if (!(passenger instanceof Player) && spec.hasTrunk()) {
            dismountCooldowns.put(passenger.getUUID(), this.tickCount + 120);
            Vec3 rearPos = this.position().add(rotateLocalOffset(spec.trunkPickupOffset().add(0.0, 0.2, -0.7), this.getYRot()));
            return rearPos;
        }
        Vec3 sideExit = this.position().add(rotateLocalOffset(new Vec3(1.6, 0.2, 0.0), this.getYRot()));
        return sideExit;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        String eggType = resolveVehicleTypeFromEgg(held);

        // 1. Vehicles Remover item removes the vehicle
        if ("remover".equals(eggType)) {
            if (!this.level().isClientSide()) {
                removeAndDropVehicle(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // 2. Sneak + Right-Click: Unload animals from trunk if any; if empty, pick up vehicle
        if (player.isSecondaryUseActive()) {
            if (!this.level().isClientSide()) {
                if (!getCargoPassengers().isEmpty()) {
                    unloadCargoAnimals(player);
                } else if (getDriver() == null) {
                    removeAndDropVehicle(player);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // 3. Normal Right-Click: Mount player into the driver seat
        if (getDriver() == null) {
            if (!this.level().isClientSide()) {
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return InteractionResult.PASS;
    }

    public void removeAndDropVehicle(Player player) {
        if (!getCargoPassengers().isEmpty()) {
            unloadCargoAnimals(player);
        }
        for (Entity passenger : new ArrayList<>(this.getPassengers())) {
            passenger.stopRiding();
        }
        if (player != null && !player.getAbilities().instabuild) {
            ItemStack dropStack = createSpawnEggForType(getVehicleType());
            if (!dropStack.isEmpty()) {
                if (!player.getInventory().add(dropStack)) {
                    this.spawnAtLocation(dropStack);
                }
            }
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8f, 1.0f);
        this.discard();
    }

    public static ItemStack createSpawnEggForType(String typeId) {
        VehicleSpec spec = getSpec(typeId);
        for (ItemStack candidate : ZooItems.createPhysicalVehiclesItems()) {
            CustomModelData cmd = candidate.get(DataComponents.CUSTOM_MODEL_DATA);
            if (cmd != null && cmd.value() == spec.eggCmd()) {
                return candidate.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (source.getEntity() instanceof Player player && player.isSecondaryUseActive()) {
            if (!this.level().isClientSide()) {
                removeAndDropVehicle(player);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("VehicleType")) {
            setVehicleType(tag.getString("VehicleType"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("VehicleType", getVehicleType());
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(2.2f, 1.6f);
    }
}
