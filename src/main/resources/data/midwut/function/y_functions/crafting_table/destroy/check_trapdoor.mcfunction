execute if block ~ ~ ~ minecraft:moving_piston run return run function midwut:y_functions/crafting_table/destroy/moving_piston
execute if block ~ ~ ~ minecraft:iron_trapdoor[waterlogged=false] run setblock ~ ~ ~ minecraft:air
execute if block ~ ~ ~ minecraft:iron_trapdoor[waterlogged=true] run setblock ~ ~ ~ minecraft:water
