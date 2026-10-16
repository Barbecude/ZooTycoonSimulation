data modify storage midwut:main Items3 append from storage midwut:main Items2[-1]
data remove storage midwut:main Items2[-1]
data modify storage midwut:main temp set from storage midwut:main Inventory[-1]
scoreboard players set #test main_score 0
execute store success score #test main_score run data modify storage midwut:main temp.tag set from storage midwut:main Items2[-1].tag
execute if score #test main_score matches 1 if data storage midwut:main Items2[1] run function midwut:y_functions/items_and_storages/clear/inventory_by_count/tag/main2