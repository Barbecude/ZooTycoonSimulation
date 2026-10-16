data modify storage midwut:main temp set from storage midwut:main Inventory[-1]
data modify storage midwut:main macros.temp set from storage midwut:main Items[-1]
function midwut:y_functions/items_and_storages/clear/list/json_element/matches_partially_nbt_tags/test with storage midwut:main macros
execute if score #test main_score matches 1 if data storage midwut:main Items[1] run function midwut:y_functions/items_and_storages/clear/list/json_element/matches_partially_nbt_tags/pre2
execute if score #test main_score matches 1 run data modify storage midwut:main Inventory2 prepend from storage midwut:main Inventory[-1]
execute if score #test main_score matches 0 run scoreboard players add #midwut.out main_score 1

data remove storage midwut:main Inventory[-1]
execute if data storage midwut:main Inventory[-1] run function midwut:y_functions/items_and_storages/clear/list/json_element/matches_partially_nbt_tags/main

