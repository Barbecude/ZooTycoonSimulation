#> midwut:help/items_and_storages/identical/inventory/id-tag

execute store result score #midwut.out main_score if data storage midwut:main Inventory[]
execute store result score #temp main_score if data storage midwut:main Items[]
execute unless score #midwut.out main_score = #temp main_score run return run scoreboard players set #midwut.out main_score 0
scoreboard players set #midwut.out main_score 1
execute if data storage midwut:main Inventory[0] if data storage midwut:main Items[0] run function midwut:y_functions/items_and_storages/identical/inventory/id-tag/loop
execute store result score #midwut.out main_score if score #midwut.out main_score matches 0
return run scoreboard players get #midwut.out main_score

