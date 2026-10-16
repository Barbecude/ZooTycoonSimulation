function midwut:y_functions/items_and_storages/identical/inventory/id-tag/loop2_pre
execute if score #midwut.out main_score matches 1 run return 0
data remove storage midwut:main Inventory[-1]
execute if data storage midwut:main Inventory[0] run function midwut:y_functions/items_and_storages/identical/inventory/id-tag/loop


