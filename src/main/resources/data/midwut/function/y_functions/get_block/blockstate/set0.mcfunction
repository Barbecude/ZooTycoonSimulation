loot replace block 0 0 0 container.0 mine 2 0 0 minecraft:shears[minecraft:enchantments={"minecraft:silk_touch":255}]
data modify storage midwut:main output_block.block.Name set from block 0 0 0 Items[0].id
execute if block 2 0 0 #midwut:wall_blocks run function midwut:y_functions/get_block/blockstate/wall_blocks/main

