#> midwut:help/items_and_storages/clear/list/json_element/matches_item_keys

scoreboard players set #midwut.out main_score 0
execute if data storage midwut:main Inventory[0] if data storage midwut:main Items[0] run function midwut:y_functions/items_and_storages/clear/list/json_element/matches_item_keys/pre
return run scoreboard players get #midwut.out main_score
