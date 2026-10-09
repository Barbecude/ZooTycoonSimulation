package com.example.simulation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;

public class PehkuiIntegration {
    public static final String MODID = "pehkui";

    public static boolean isPehkuiLoaded() {
        return ModList.get().isLoaded(MODID);
    }

    public static void resizeEntity(LivingEntity entity, float scale) {
        if (!isPehkuiLoaded())
            return;

        try {
            // Menggunakan Reflection untuk akses API Pehkui
            // virtuoel.pehkui.api.ScaleTypes.BASE.getScaleData(entity).setScale(scale);

            Class<?> scaleTypesClass = Class.forName("virtuoel.pehkui.api.ScaleTypes");
            Object baseType = scaleTypesClass.getField("BASE").get(null);

            Class<?> scaleTypeClass = Class.forName("virtuoel.pehkui.api.ScaleType");
            Object scaleData = scaleTypeClass.getMethod("getScaleData", net.minecraft.world.entity.Entity.class)
                    .invoke(baseType, entity);

            Class<?> scaleDataClass = Class.forName("virtuoel.pehkui.api.ScaleData");
            // Set target scale (akan transisi smooth) dan scale instan
            scaleDataClass.getMethod("setTargetScale", float.class).invoke(scaleData, scale);
            scaleDataClass.getMethod("setScale", float.class).invoke(scaleData, scale);

        } catch (Exception e) {
            // Silent error or debug
        }
    }
}
