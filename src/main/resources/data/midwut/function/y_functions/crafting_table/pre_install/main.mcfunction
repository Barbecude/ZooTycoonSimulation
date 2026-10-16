
execute store result score #temp main_score run data get entity @a[tag=midwut.used.dolphin_spawn_egg.selected_player,limit=1] Rotation[0] 10
execute if score #temp main_score matches ..-446 run scoreboard players add #temp main_score 3600
scoreboard players add #temp main_score 445
scoreboard players operation #temp main_score /= #900 const

function midwut:y_functions/crafting_table/pre_install/main_return

execute if entity @e[type=minecraft:block_display,tag=midwut.ghost_block.crafting_table,tag=midwut.ghost_block.setup,limit=1,distance=..2.24] run function midwut:y_functions/crafting_table/pre_install/ghost_blocks
