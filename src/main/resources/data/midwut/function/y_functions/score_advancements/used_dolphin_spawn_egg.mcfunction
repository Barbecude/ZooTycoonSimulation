advancement revoke @s only midwut:scores/used_dolphin_spawn_egg

tag @s add midwut.used.dolphin_spawn_egg.selected_player
function #midwut:used/dolphin_spawn_egg
execute as @e[type=minecraft:marker,tag=midwut.crafting_table.spawn,x=0] at @s run function midwut:y_functions/crafting_table/pre_install/pre
tag @s remove midwut.used.dolphin_spawn_egg.selected_player

scoreboard players reset @s midwut.used.dolphin_spawn_egg
