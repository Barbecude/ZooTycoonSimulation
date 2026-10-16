#> midwut:y_functions/crafting_table/main
execute store result score #update_data main_score run function midwut:y_functions/crafting_table/kill_items
execute store result storage midwut:main macros.id int 1 run scoreboard players get @s midwut.crafting_table.id
execute if block ~ ~ ~ minecraft:barrel run return run function midwut:y_functions/crafting_table/test
function midwut:y_functions/crafting_table/destroy/main with storage midwut:main macros
