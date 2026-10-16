execute as @e[type=minecraft:item,distance=..2,nbt={Age:0s,OnGround:0b}] if items entity @s contents minecraft:iron_trapdoor run return run function midwut:y_functions/crafting_table/test_blocks/kill_trapdoor
execute if function midwut:y_functions/crafting_table/test_blocks/test_item_trapdoor/check_moving_piston run return fail
execute if block ~ ~ ~ #minecraft:air run return run setblock ~ ~ ~ minecraft:iron_trapdoor[facing=north,open=true,waterlogged=false]
execute if block ~ ~ ~ minecraft:water run return run setblock ~ ~ ~ minecraft:iron_trapdoor[facing=north,open=true,waterlogged=true]
return fail