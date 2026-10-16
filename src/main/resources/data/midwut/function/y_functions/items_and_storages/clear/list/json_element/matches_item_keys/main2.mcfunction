data remove storage midwut:main Items2[-1]
data modify storage midwut:main temp set from storage midwut:main Inventory[-1]
scoreboard players set #test main_score -1
execute if data storage midwut:main Items[-1].id run function midwut:y_functions/items_and_storages/clear/list/json_element/matches_item_keys/id
execute if score #test main_score matches ..0 if data storage midwut:main Items[-1].Count run function midwut:y_functions/items_and_storages/clear/list/json_element/matches_item_keys/count
execute if score #test main_score matches ..0 if data storage midwut:main Items[-1].tag run function midwut:y_functions/items_and_storages/clear/list/json_element/matches_item_keys/tag

execute unless score #test main_score matches 0 if data storage midwut:main Items2[1] run function midwut:y_functions/items_and_storages/clear/list/json_element/matches_item_keys/main2