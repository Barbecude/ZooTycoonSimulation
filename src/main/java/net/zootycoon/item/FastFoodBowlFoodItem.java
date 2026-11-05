package net.zootycoon.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class FastFoodBowlFoodItem extends Item {
    private final Supplier<Item> remainderSupplier;

    public FastFoodBowlFoodItem(Properties properties, Supplier<Item> remainderSupplier) {
        super(properties);
        this.remainderSupplier = remainderSupplier;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        super.finishUsingItem(stack, level, livingEntity);
        Item remainder = remainderSupplier != null ? remainderSupplier.get() : null;
        if (remainder != null && remainder != Items.AIR) {
            if (stack.isEmpty()) {
                return new ItemStack(remainder);
            } else {
                if (livingEntity instanceof Player player && !player.getAbilities().instabuild) {
                    ItemStack remStack = new ItemStack(remainder);
                    if (!player.getInventory().add(remStack)) {
                        player.drop(remStack, false);
                    }
                }
            }
        }
        return stack;
    }
}
