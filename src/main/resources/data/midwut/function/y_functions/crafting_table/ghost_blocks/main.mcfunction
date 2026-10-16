scoreboard players add @s midwut.crafting_table.page 1
execute if score @s midwut.crafting_table.page matches 5.. run return run kill @s

execute if entity @s[tag=!midwut.ghost_block.checked] run return run function midwut:y_functions/crafting_table/ghost_blocks/glow_false
data modify entity @s Glowing set value true
tag @s remove midwut.ghost_block.checked
