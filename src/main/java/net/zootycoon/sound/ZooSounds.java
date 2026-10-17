package net.zootycoon.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.zootycoon.ZooTycoonMod;

public class ZooSounds {
    public static final ResourceLocation KACHING_ID = ZooTycoonMod.id("kaching");
    public static final SoundEvent KACHING = SoundEvent.createVariableRangeEvent(KACHING_ID);

    public static void register() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, KACHING_ID, KACHING);
    }
}

