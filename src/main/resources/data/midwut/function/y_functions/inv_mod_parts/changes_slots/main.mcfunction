execute store result score #temp main_score run data get storage midwut:main Inventory2[-1].Slot
execute if score #temp main_score matches 0..8 run data modify storage midwut:main hotbar append from storage midwut:main Inventory2[-1]
execute if score #temp main_score matches 9..99 run function midwut:y_functions/inv_mod_parts/changes_slots/inventory

data remove storage midwut:main Inventory2[-1]
execute if data storage midwut:main Inventory2[0] run function midwut:y_functions/inv_mod_parts/changes_slots/main


