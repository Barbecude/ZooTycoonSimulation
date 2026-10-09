package net.zootycoon.schematic;

import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;

public class ZooSchematic {
    private final String id;
    private final String name;
    private final String category; // "enclosures", "essentials", or custom
    private final String subcategory;
    private final String author;
    private final Vec3i dimensions;
    private final String description;
    private final int builderHiringCost;
    private final int defaultPrice;
    private final boolean isCustom;
    private final ResourceLocation structureLocation;

    public ZooSchematic(String id, String name, String category, String subcategory, String author,
                        Vec3i dimensions, String description, int builderHiringCost, int defaultPrice,
                        boolean isCustom, ResourceLocation structureLocation) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.subcategory = subcategory;
        this.author = author;
        this.dimensions = dimensions;
        this.description = description;
        this.builderHiringCost = builderHiringCost;
        this.defaultPrice = defaultPrice;
        this.isCustom = isCustom;
        this.structureLocation = structureLocation;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public String getAuthor() {
        return author;
    }

    public Vec3i getDimensions() {
        return dimensions;
    }

    public String getDescription() {
        return description;
    }

    public int getBuilderHiringCost() {
        return builderHiringCost;
    }

    public int getDefaultPrice() {
        return defaultPrice;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public ResourceLocation getStructureLocation() {
        return structureLocation;
    }
}

