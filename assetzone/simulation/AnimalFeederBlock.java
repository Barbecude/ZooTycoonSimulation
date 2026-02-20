package com.example.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AnimalFeederBlock extends BaseEntityBlock {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 4);

    public AnimalFeederBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5F)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AnimalFeederBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AnimalFeederBlockEntity feeder)) return InteractionResult.PASS;

        if (!level.isClientSide) {
            ItemStack held = player.getItemInHand(hand);
            if (!held.isEmpty()) {
                String assignedAnimal = feeder.getAssignedAnimalId();
                boolean inserted = feeder.addFood(held, 20, assignedAnimal);
                if (inserted) {
                    if (!player.getAbilities().instabuild) held.shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
            String info = "Feeder Stock: " + feeder.getFoodLevel() + "%";
            if (!feeder.getAssignedAnimalId().isEmpty()) {
                info += " | For: " + feeder.getAssignedAnimalId();
            }
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(info));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(LEVEL);
    }
}
