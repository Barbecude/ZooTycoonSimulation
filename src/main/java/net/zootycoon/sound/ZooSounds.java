package net.zootycoon.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.zootycoon.ZooTycoonMod;

public class ZooSounds {
    public static final ResourceLocation KACHING_ID = ZooTycoonMod.id("kaching");
    public static final SoundEvent KACHING = SoundEvent.createVariableRangeEvent(KACHING_ID);

    public static final SoundEvent VEH_KEY_INSERT = createPhysVeh("other.car_key_insert_into_ignition");
    public static final SoundEvent VEH_KEY_REMOVE = createPhysVeh("other.car_key_remove_from_ignition");
    public static final SoundEvent VEH_IGNITION_START = createPhysVeh("other.ignition_start");
    public static final SoundEvent VEH_IGNITION_STARTING = createPhysVeh("other.ignition_starting");
    public static final SoundEvent VEH_ENGINE_0 = createPhysVeh("other.engine.0");
    public static final SoundEvent VEH_ENGINE_1 = createPhysVeh("other.engine.1");
    public static final SoundEvent VEH_ENGINE_2 = createPhysVeh("other.engine.2");
    public static final SoundEvent VEH_ZAZ_ENGINE = createPhysVeh("vehicle.zaz-968m.engine");
    public static final SoundEvent VEH_ZAZ_IGNITION_START = createPhysVeh("vehicle.zaz-968m.ignition_start");
    public static final SoundEvent VEH_TIRES_HARD = createPhysVeh("other.tires.1");
    public static final SoundEvent VEH_TIRES_ROUGH = createPhysVeh("other.tires.2");
    public static final SoundEvent VEH_TIRES_LOOSE = createPhysVeh("other.tires.3");
    public static final SoundEvent VEH_TIRES_FRIABLE = createPhysVeh("other.tires.4");
    public static final SoundEvent VEH_HANDBRAKE_ON = createPhysVeh("other.handbrake_tightened");
    public static final SoundEvent VEH_HANDBRAKE_OFF = createPhysVeh("other.handbrake_removed");
    public static final SoundEvent VEH_TRUCK_HANDBRAKE_ON = createPhysVeh("other.handbrake_truck_tightened");
    public static final SoundEvent VEH_TRUCK_HANDBRAKE_OFF = createPhysVeh("other.handbrake_truck_removed");
    public static final SoundEvent VEH_CRASH = createPhysVeh("other.crash");
    public static final SoundEvent VEH_HORN_1 = createPhysVeh("other.horn.1");
    public static final SoundEvent VEH_HORN_2 = createPhysVeh("other.horn.2");
    public static final SoundEvent VEH_GEAR_START = createPhysVeh("other.switching_gear.start");
    public static final SoundEvent VEH_GEAR_FINISH = createPhysVeh("other.switching_gear.finish");

    private static SoundEvent createPhysVeh(String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("physical_vehicles", path);
        return SoundEvent.createVariableRangeEvent(id);
    }

    private static void reg(SoundEvent event) {
        Registry.register(BuiltInRegistries.SOUND_EVENT, event.getLocation(), event);
    }

    public static void register() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, KACHING_ID, KACHING);
        reg(VEH_KEY_INSERT);
        reg(VEH_KEY_REMOVE);
        reg(VEH_IGNITION_START);
        reg(VEH_IGNITION_STARTING);
        reg(VEH_ENGINE_0);
        reg(VEH_ENGINE_1);
        reg(VEH_ENGINE_2);
        reg(VEH_ZAZ_ENGINE);
        reg(VEH_ZAZ_IGNITION_START);
        reg(VEH_TIRES_HARD);
        reg(VEH_TIRES_ROUGH);
        reg(VEH_TIRES_LOOSE);
        reg(VEH_TIRES_FRIABLE);
        reg(VEH_HANDBRAKE_ON);
        reg(VEH_HANDBRAKE_OFF);
        reg(VEH_TRUCK_HANDBRAKE_ON);
        reg(VEH_TRUCK_HANDBRAKE_OFF);
        reg(VEH_CRASH);
        reg(VEH_HORN_1);
        reg(VEH_HORN_2);
        reg(VEH_GEAR_START);
        reg(VEH_GEAR_FINISH);
    }
}
