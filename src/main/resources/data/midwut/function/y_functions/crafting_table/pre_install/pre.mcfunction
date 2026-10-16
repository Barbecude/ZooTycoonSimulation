#execute store result score #y main_score run data get entity @s Pos[1] 10
#scoreboard players operation #y main_score %= #10 const

execute align xyz if entity @s[dx=0] positioned as @s run return run function midwut:y_functions/crafting_table/destroy2
function midwut:y_functions/crafting_table/pre_install/main


