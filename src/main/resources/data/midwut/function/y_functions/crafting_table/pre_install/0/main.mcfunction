execute if block ~-1 ~ ~ minecraft:chest run return run function midwut:y_functions/crafting_table/pre_install/0/main_mirror

execute positioned ~1 ~ ~ unless block ~ ~ ~ minecraft:chest[facing=north] run function midwut:y_functions/crafting_table/pre_install/0/chest
execute positioned ~1 ~ ~1 unless block ~ ~ ~ minecraft:iron_trapdoor[open=false,half=bottom] run function midwut:y_functions/crafting_table/pre_install/block/iron_trapdoor
execute positioned ~1 ~ ~2 unless block ~ ~ ~ minecraft:iron_trapdoor[open=false,half=bottom] run function midwut:y_functions/crafting_table/pre_install/block/iron_trapdoor
execute positioned ~ ~ ~1 unless block ~ ~ ~ minecraft:iron_trapdoor[open=false,half=bottom] run function midwut:y_functions/crafting_table/pre_install/block/iron_trapdoor
execute positioned ~ ~ ~2 unless block ~ ~ ~ minecraft:iron_trapdoor[open=false,half=bottom] run function midwut:y_functions/crafting_table/pre_install/block/iron_trapdoor
execute positioned ~ ~1 ~ unless block ~ ~ ~ #midwut:air_and_water run function midwut:y_functions/crafting_table/pre_install/block/error

execute unless entity @e[type=minecraft:block_display,tag=midwut.ghost_block.crafting_table,tag=midwut.ghost_block.setup,limit=1,distance=..2.24] run function midwut:y_functions/crafting_table/install
