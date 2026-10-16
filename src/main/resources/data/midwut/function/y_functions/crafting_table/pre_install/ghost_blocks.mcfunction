function midwut:y_functions/crafting_table/destroy2
execute unless entity @e[type=minecraft:block_display,tag=midwut.ghost_block.crafting_table,tag=!midwut.ghost_block.setup] run schedule function midwut:y_functions/crafting_table/ghost_blocks/tick 8t replace
tag @e[type=minecraft:block_display,tag=midwut.ghost_block.crafting_table,tag=midwut.ghost_block.setup,distance=..2.24] remove midwut.ghost_block.setup
