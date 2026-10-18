package net.zootycoon.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.zootycoon.block.ZooBlocks;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Block entity managing burn/cooling duration for Heater and Cooler Box blocks.
 * Maintains thread-safe spatial registries of active heaters and coolers for instant climate queries.
 */
public class ZooThermalBlockEntity extends BlockEntity {

    public enum ThermalType {
        HEATING,
        COOLING
    }

    public static final Set<BlockPos> ACTIVE_HEATERS = ConcurrentHashMap.newKeySet();
    public static final Set<BlockPos> ACTIVE_COOLERS = ConcurrentHashMap.newKeySet();

    private final ThermalType thermalType;
    private int fuelTime = 0;
    private int maxFuelTime = 0;

    public ZooThermalBlockEntity(BlockPos pos, BlockState state, ThermalType thermalType) {
        super(thermalType == ThermalType.HEATING ? ZooBlocks.HEATER_BLOCK_ENTITY : ZooBlocks.COOLER_BLOCK_ENTITY, pos, state);
        this.thermalType = thermalType;
    }

    public ThermalType getThermalType() {
        return thermalType;
    }

    public int getFuelTime() {
        return fuelTime;
    }

    public int getMaxFuelTime() {
        return maxFuelTime;
    }

    public boolean isLit() {
        return fuelTime > 0;
    }

    public void addFuel(int ticks) {
        this.fuelTime += ticks;
        if (this.fuelTime > this.maxFuelTime) {
            this.maxFuelTime = this.fuelTime;
        }
        updateLitState();
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ZooThermalBlockEntity blockEntity) {
        if (level == null) return;

        if (blockEntity.fuelTime > 0) {
            blockEntity.fuelTime--;
            if (blockEntity.thermalType == ThermalType.HEATING) {
                ACTIVE_HEATERS.add(pos.immutable());
            } else {
                ACTIVE_COOLERS.add(pos.immutable());
            }

            if (blockEntity.fuelTime <= 0) {
                blockEntity.fuelTime = 0;
                blockEntity.maxFuelTime = 0;
                blockEntity.updateLitState();
                if (blockEntity.thermalType == ThermalType.HEATING) {
                    ACTIVE_HEATERS.remove(pos);
                } else {
                    ACTIVE_COOLERS.remove(pos);
                }
            }
            blockEntity.setChanged();
        } else {
            if (blockEntity.thermalType == ThermalType.HEATING) {
                ACTIVE_HEATERS.remove(pos);
            } else {
                ACTIVE_COOLERS.remove(pos);
            }
        }
    }

    private void updateLitState() {
        if (level == null) return;
        BlockState currentState = getBlockState();
        boolean shouldBeLit = this.fuelTime > 0;
        if (currentState.hasProperty(BlockStateProperties.LIT) && currentState.getValue(BlockStateProperties.LIT) != shouldBeLit) {
            level.setBlock(worldPosition, currentState.setValue(BlockStateProperties.LIT, shouldBeLit), 3);
        }
    }

    @Override
    public void setRemoved() {
        if (thermalType == ThermalType.HEATING) {
            ACTIVE_HEATERS.remove(worldPosition);
        } else {
            ACTIVE_COOLERS.remove(worldPosition);
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("FuelTime", fuelTime);
        tag.putInt("MaxFuelTime", maxFuelTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.fuelTime = tag.getInt("FuelTime");
        this.maxFuelTime = tag.getInt("MaxFuelTime");
        if (this.fuelTime > 0) {
            if (thermalType == ThermalType.HEATING) {
                ACTIVE_HEATERS.add(worldPosition.immutable());
            } else {
                ACTIVE_COOLERS.add(worldPosition.immutable());
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("FuelTime", fuelTime);
        tag.putInt("MaxFuelTime", maxFuelTime);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

