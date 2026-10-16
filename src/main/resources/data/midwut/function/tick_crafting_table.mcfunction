execute as @e[type=minecraft:marker,tag=midwut.crafting_table] at @s run function midwut:y_functions/crafting_table/main

execute if data storage midwut:main midwut_crafting_tables[0] run schedule function midwut:tick_crafting_table 1t replace
