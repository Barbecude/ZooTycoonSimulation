package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class ZooComputerBlockEntity extends BlockEntity implements MenuProvider {
    private int scanRadius = 20;
    private int tickCounter = 0;
    private int cachedAnimalCount = 0;
    private int cachedStaffCount = 0;
    private int cachedVisitorCount = 0;
    private int cachedTrashCount = 0;
    private boolean hasGate = false;

    // Zone system (custom boundaries)
    private BlockPos zoneMin = null;
    private BlockPos zoneMax = null;
    private boolean useCustomZone = false;

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            int bal = getBalanceGlobal();
            return switch (index) {
                case 0 -> (bal >> 16) & 0xFFFF;
                case 1 -> bal & 0xFFFF;
                case 2 -> cachedAnimalCount;
                case 3 -> cachedStaffCount;
                case 4 -> cachedVisitorCount;
                case 5 -> cachedTrashCount;
                case 6 -> scanRadius;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    public ZooComputerBlockEntity(BlockPos pos, BlockState state) {
        super(IndoZooTycoon.ZOO_COMPUTER_BE.get(), pos, state);
    }

    // Helper to access global data safely
    private int getBalanceGlobal() {
        if (level instanceof ServerLevel sl) {
            return ZooData.get(sl).getBalance();
        }
        return 0; // Client-side BE doesn't track balance directly
    }

    private void addBalanceGlobal(int amount) {
        if (level instanceof ServerLevel sl) {
            ZooData.get(sl).addBalance(amount);
        }
    }

    private void setBalanceGlobal(int amount) {
        if (level instanceof ServerLevel sl) {
            ZooData.get(sl).setBalance(amount);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("IndoZoo Dashboard");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ZooComputerMenu(id, inv, worldPosition, containerData);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        // Balance saved in Global ZooData, not here
        tag.putInt("ScanRadius", scanRadius);
        tag.putBoolean("UseCustomZone", useCustomZone);
        if (zoneMin != null) {
            tag.putLong("ZoneMin", zoneMin.asLong());
        }
        if (zoneMax != null) {
            tag.putLong("ZoneMax", zoneMax.asLong());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        // Balance loaded from Global ZooData automatically on access
        scanRadius = tag.getInt("ScanRadius");
        useCustomZone = tag.getBoolean("UseCustomZone");
        if (tag.contains("ZoneMin")) {
            zoneMin = BlockPos.of(tag.getLong("ZoneMin"));
        }
        if (tag.contains("ZoneMax")) {
            zoneMax = BlockPos.of(tag.getLong("ZoneMax"));
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ZooComputerBlockEntity be) {
        if (level.isClientSide)
            return;

        be.tickCounter++;

        // Particle visualization (setiap 10 ticks = 0.5 detik)
        if (be.tickCounter % 10 == 0) {
            be.drawBoundaryParticles((ServerLevel) level);
        }

        // Automatic Update (Every 20 seconds / 400 ticks)
        if (be.tickCounter % 400 == 0) {
            be.forceUpdate();
        }

        // Update Block State
        boolean active = be.cachedAnimalCount > 0;
        if (state.getValue(ZooComputerBlock.ACTIVE) != active) {
            level.setBlock(pos, state.setValue(ZooComputerBlock.ACTIVE, active), 3);
        }

        // Spawn Visitor di Gate (BALANCED SPAWN RATE)
        // Visitor Spawning Logic moved to forceUpdate()

        // The following code block was moved into forceUpdate()
        // if (be.hasGate && be.cachedAnimalCount >= 2 && be.cachedVisitorCount <
        // targetVisitors
        // && be.cachedVisitorCount < maxVisitors) {
        // VisitorEntity visitor = IndoZooTycoon.VISITOR_ENTITY.get().create(server);
        // if (visitor != null) {
        // visitor.setGatePos(gatePos);
        // visitor.moveTo(gatePos.getX() + 0.5, gatePos.getY(), gatePos.getZ() + 0.5, 0,
        // 0);
        // server.addFreshEntity(visitor);
        // }
        // }

        // FORCE visitors to leave if timeout (cleanup stuck visitors)
        // The following code block was moved into forceUpdate()
        // for (VisitorEntity v : server.getEntitiesOfClass(VisitorEntity.class, area))
        // {
        // if (v.isTimeToLeave()) {
        // v.forceLeave(); // Force navigation to gate
        // }
        // }
    }

    /**
     * Triggered manually by Refresh Button (Packet) or automatically
     */
    public void forceUpdate() {
        if (level == null || level.isClientSide)
            return;
        ServerLevel server = (ServerLevel) level;

        // Scan for Zone Markers
        scanForZoneMarkers(server, worldPosition);
        AABB area = getScanArea(worldPosition);

        // Check Gate
        BlockPos gatePos = null;
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        for (int x = (int) area.minX; x <= area.maxX; x++) {
            for (int y = (int) area.minY; y <= area.maxY; y++) {
                for (int z = (int) area.minZ; z <= area.maxZ; z++) {
                    mPos.set(x, y, z);
                    if (level.getBlockState(mPos).getBlock() instanceof BannerBlock) {
                        gatePos = mPos.immutable();
                        break;
                    }
                }
                if (gatePos != null)
                    break;
            }
            if (gatePos != null)
                break;
        }
        this.hasGate = gatePos != null;

        // Scan Assets
        this.cachedAnimalCount = server.getEntitiesOfClass(Animal.class, area).size();
        this.cachedStaffCount = server.getEntitiesOfClass(StaffEntity.class, area).size();
        this.cachedVisitorCount = server.getEntitiesOfClass(VisitorEntity.class, area).size();

        // Constrain Staff
        int restrictionRadius = scanRadius;
        if (useCustomZone && zoneMin != null && zoneMax != null) {
            double d1 = Math.sqrt(worldPosition.distSqr(zoneMin));
            double d2 = Math.sqrt(worldPosition.distSqr(zoneMax));
            restrictionRadius = (int) Math.ceil(Math.max(d1, d2));
        }

        for (StaffEntity staff : server.getEntitiesOfClass(StaffEntity.class, area)) {
            if (!area.contains(staff.position())) {
                staff.teleportTo(worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5);
            }
            staff.setHomePos(worldPosition, restrictionRadius);
        }

        // Trash
        int trash = 0;
        for (ItemEntity ie : server.getEntitiesOfClass(ItemEntity.class, area)) {
            if (ie.getItem().getHoverName().getString().contains("Sampah"))
                trash++;
        }
        this.cachedTrashCount = trash;

        // Economy
        int profit = (this.cachedAnimalCount * 500) - (this.cachedStaffCount * 200) - (this.cachedTrashCount * 100);
        addBalanceGlobal(profit);

        // Visitor Spawning Logic
        if (this.hasGate && this.cachedAnimalCount >= 2 && this.cachedVisitorCount < (this.cachedAnimalCount / 2)
                && this.cachedVisitorCount < Math.min(this.cachedAnimalCount * 2, 20)) {
            VisitorEntity visitor = IndoZooTycoon.VISITOR_ENTITY.get().create(server);
            if (visitor != null) {
                // Must handle if gatePos is null (e.g. gate removed)
                if (gatePos != null) {
                    visitor.setGatePos(gatePos);
                    visitor.moveTo(gatePos.getX() + 0.5, gatePos.getY(), gatePos.getZ() + 0.5, 0, 0);
                    server.addFreshEntity(visitor);
                }
            }
        }

        // Cleanup Visitors
        for (VisitorEntity v : server.getEntitiesOfClass(VisitorEntity.class, area)) {
            if (v.isTimeToLeave())
                v.forceLeave();
        }

        setChanged();
        // Force Block Update to notify listeners/containers
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /**
     * Scan area untuk 2 Zone Marker blocks (define custom boundary)
     */
    private void scanForZoneMarkers(ServerLevel level, BlockPos center) {
        List<BlockPos> markers = new ArrayList<>();
        int searchRadius = 50; // Scan 50 blok untuk cari markers

        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-searchRadius, -10, -searchRadius),
                center.offset(searchRadius, 10, searchRadius))) {
            if (level.getBlockState(p).getBlock() instanceof ZoneMarkerBlock) {
                markers.add(p.immutable());
            }
        }

        // Jika ada minimal 2 markers, gunakan sebagai custom zone
        if (markers.size() >= 2) {
            BlockPos p1 = markers.get(0);
            BlockPos p2 = markers.get(1);

            // Calculate bounding box dari 2 corners
            int minX = Math.min(p1.getX(), p2.getX());
            int minY = Math.min(p1.getY(), p2.getY());
            int minZ = Math.min(p1.getZ(), p2.getZ());
            int maxX = Math.max(p1.getX(), p2.getX());
            int maxY = Math.max(p1.getY(), p2.getY());
            int maxZ = Math.max(p1.getZ(), p2.getZ());

            zoneMin = new BlockPos(minX, minY, minZ);
            zoneMax = new BlockPos(maxX, maxY, maxZ);
            useCustomZone = true;
        } else {
            useCustomZone = false;
        }
    }

    /**
     * Get scan area (custom zone atau fallback ke radius)
     */
    private AABB getScanArea(BlockPos center) {
        if (useCustomZone && zoneMin != null && zoneMax != null) {
            return new AABB(zoneMin, zoneMax);
        } else {
            // Fallback: kotak dengan radius
            return new AABB(center).inflate(scanRadius);
        }
    }

    /**
     * Draw particle boundary untuk visualisasi (agar keliatan bordernya)
     */
    private void drawBoundaryParticles(ServerLevel level) {
        AABB area = getScanArea(worldPosition);

        int minX = (int) area.minX;
        int minY = (int) area.minY;
        int minZ = (int) area.minZ;
        int maxX = (int) area.maxX;
        int maxY = (int) area.maxY;
        int maxZ = (int) area.maxZ;

        // Draw particles di 4 vertical edges (corner posts)
        spawnVerticalParticleLine(level, minX, minY, maxY, minZ);
        spawnVerticalParticleLine(level, maxX, minY, maxY, minZ);
        spawnVerticalParticleLine(level, minX, minY, maxY, maxZ);
        spawnVerticalParticleLine(level, maxX, minY, maxY, maxZ);

        // Draw horizontal lines di ground level (agar keliatan batas floor)
        spawnHorizontalParticleLineX(level, minX, maxX, minY, minZ);
        spawnHorizontalParticleLineX(level, minX, maxX, minY, maxZ);
        spawnHorizontalParticleLineZ(level, minX, minY, minZ, maxZ);
        spawnHorizontalParticleLineZ(level, maxX, minY, minZ, maxZ);
    }

    private void spawnVerticalParticleLine(ServerLevel level, int x, int yMin, int yMax, int z) {
        for (int y = yMin; y <= yMax; y += 2) {
            level.sendParticles(ParticleTypes.END_ROD,
                    x + 0.5, y + 0.5, z + 0.5,
                    1, 0, 0, 0, 0);
        }
    }

    private void spawnHorizontalParticleLineX(ServerLevel level, int x1, int x2, int y, int z) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x += 2) {
            level.sendParticles(ParticleTypes.END_ROD,
                    x + 0.5, y + 0.5, z + 0.5,
                    1, 0, 0, 0, 0);
        }
    }

    private void spawnHorizontalParticleLineZ(ServerLevel level, int x, int y, int z1, int z2) {
        for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z += 2) {
            level.sendParticles(ParticleTypes.END_ROD,
                    x + 0.5, y + 0.5, z + 0.5,
                    1, 0, 0, 0, 0);
        }
    }

    public void resetProgress() {
        setBalanceGlobal(5000);
        this.scanRadius = 20;
        this.useCustomZone = false;
        this.zoneMin = null;
        this.zoneMax = null;
        if (level instanceof ServerLevel server) {
            AABB area = new AABB(worldPosition).inflate(100);
            List<StaffEntity> staff = server.getEntitiesOfClass(StaffEntity.class, area);
            for (StaffEntity s : staff)
                s.discard();
        }
        setChanged();
    }

    public int getBalance() {
        return getBalanceGlobal();
    }

    public void addBalance(int amount) {
        addBalanceGlobal(amount);
        setChanged();
    }

    public int getScanRadius() {
        return scanRadius;
    }

    public void setScanRadius(int r) {
        this.scanRadius = r;
        setChanged();
    }

    public static int getFoodCost() {
        return 100;
    }

    public AABB getZoneBounds() {
        return getScanArea(worldPosition);
    }
}
