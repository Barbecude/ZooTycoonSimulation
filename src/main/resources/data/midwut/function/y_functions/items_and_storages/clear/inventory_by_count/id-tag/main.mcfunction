data modify storage midwut:main temp set from storage midwut:main Inventory[-1]
data modify storage midwut:main Items3 set value []
scoreboard players set #test main_score 0
execute store success score #test main_score run data modify storage midwut:main temp.id set from storage midwut:main Items2[-1].id
execute if score #test main_score matches 0 run function midwut:y_functions/items_and_storages/clear/inventory_by_count/id-tag/tag with storage midwut:main Items2[-1]
execute if score #test main_score matches 1 if data storage midwut:main Items2[1] run function midwut:y_functions/items_and_storages/clear/inventory_by_count/id-tag/main2 with storage midwut:main Items2[-1]
execute if score #test main_score matches 1 run data modify storage midwut:main Inventory2 prepend from storage midwut:main Inventory[-1]
execute if score #test main_score matches 0 run function midwut:y_functions/items_and_storages/clear/inventory_by_count/remove_count

data modify storage midwut:main Items2 append from storage midwut:main Items3[]
data remove storage midwut:main Inventory[-1]
execute if data storage midwut:main Inventory[-1] run function midwut:y_functions/items_and_storages/clear/inventory_by_count/id-tag/main

