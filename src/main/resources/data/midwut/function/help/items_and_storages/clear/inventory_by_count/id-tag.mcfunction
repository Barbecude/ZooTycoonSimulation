#> midwut:help/items_and_storages/clear/inventory_by_count/id-tag

scoreboard players set #midwut.out main_score 0
execute if data storage midwut:main Inventory[0] if data storage midwut:main Items[0] run function midwut:y_functions/items_and_storages/clear/inventory_by_count/id-tag/pre
return run scoreboard players get #midwut.out main_score

