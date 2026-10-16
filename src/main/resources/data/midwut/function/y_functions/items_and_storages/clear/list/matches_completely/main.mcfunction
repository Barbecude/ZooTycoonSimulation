
#data modify storage midwut:main temp set from storage 
data modify storage midwut:main temp set from storage midwut:main Inventory[-1]
scoreboard players set #test main_score 0
execute store success score #test main_score run data modify storage midwut:main temp set from storage midwut:main Items[-1]
execute if score #test main_score matches 1 if data storage midwut:main Items[1] run function midwut:y_functions/items_and_storages/clear/list/matches_completely/pre2
execute if score #test main_score matches 1 run data modify storage midwut:main Inventory2 prepend from storage midwut:main Inventory[-1]
execute if score #test main_score matches 0 run scoreboard players add #midwut.out main_score 1

data remove storage midwut:main Inventory[-1]
execute if data storage midwut:main Inventory[-1] run function midwut:y_functions/items_and_storages/clear/list/matches_completely/main

