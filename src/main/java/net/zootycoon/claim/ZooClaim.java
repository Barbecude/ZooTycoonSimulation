package net.zootycoon.claim;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.UUID;

public class ZooClaim {
    private final UUID claimId;
    private final UUID ownerUuid;
    private final String ownerName;
    private final BoundingBox box;
    private final ClaimTier tier;
    private final long createdAt;

    public ZooClaim(UUID claimId, UUID ownerUuid, String ownerName, BoundingBox box, ClaimTier tier, long createdAt) {
        this.claimId = claimId;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.box = box;
        this.tier = tier;
        this.createdAt = createdAt;
    }

    public static ZooClaim create(UUID ownerUuid, String ownerName, BlockPos center, ClaimTier tier) {
        int halfW = tier.getWidth() / 2;
        int halfL = tier.getLength() / 2;
        BoundingBox box = new BoundingBox(
                center.getX() - halfW,
                -64,
                center.getZ() - halfL,
                center.getX() + halfW,
                320,
                center.getZ() + halfL
        );
        return new ZooClaim(UUID.randomUUID(), ownerUuid, ownerName, box, tier, System.currentTimeMillis());
    }

    public boolean contains(BlockPos pos) {
        return box.isInside(pos);
    }

    public boolean contains(int x, int z) {
        return x >= box.minX() && x <= box.maxX() && z >= box.minZ() && z <= box.maxZ();
    }

    public boolean intersects(ZooClaim other) {
        return box.intersects(other.box);
    }

    public boolean intersects(BoundingBox otherBox) {
        return box.intersects(otherBox);
    }

    public UUID getClaimId() {
        return claimId;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public BoundingBox getBox() {
        return box;
    }

    public ClaimTier getTier() {
        return tier;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("claimId", claimId);
        tag.putUUID("ownerUuid", ownerUuid);
        tag.putString("ownerName", ownerName);
        tag.putInt("minX", box.minX());
        tag.putInt("maxX", box.maxX());
        tag.putInt("minZ", box.minZ());
        tag.putInt("maxZ", box.maxZ());
        tag.putInt("minY", box.minY());
        tag.putInt("maxY", box.maxY());
        tag.putString("tier", tier.name());
        tag.putLong("createdAt", createdAt);
        return tag;
    }

    public static ZooClaim fromNbt(CompoundTag tag) {
        UUID claimId = tag.getUUID("claimId");
        UUID ownerUuid = tag.getUUID("ownerUuid");
        String ownerName = tag.getString("ownerName");
        int minX = tag.getInt("minX");
        int maxX = tag.getInt("maxX");
        int minZ = tag.getInt("minZ");
        int maxZ = tag.getInt("maxZ");
        int minY = tag.getInt("minY");
        int maxY = tag.getInt("maxY");
        BoundingBox box = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        ClaimTier tier = ClaimTier.valueOf(tag.getString("tier"));
        long createdAt = tag.getLong("createdAt");
        return new ZooClaim(claimId, ownerUuid, ownerName, box, tier, createdAt);
    }
}

