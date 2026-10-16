$data modify storage midwut:main temp set from storage midwut:main Tabs[$(temp)]

execute if data storage midwut:main temp{Slot:-1b} store result storage midwut:main temp.Slot byte 1 run scoreboard players add #test2 main_score 1
data modify block ~ ~ ~ Items append from storage midwut:main temp

scoreboard players remove #test main_score 1
execute store result storage midwut:main macros.temp int 1 run scoreboard players add #page main_score 1
execute if score #test main_score matches 1.. run function midwut:y_functions/crafting_table/custom_tabs/add with storage midwut:main macros


