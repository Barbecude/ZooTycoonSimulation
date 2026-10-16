data modify storage midwut:main temp set from storage midwut:main Inventory[-1]
function midwut:y_functions/items_and_storages/clear/inventory_by_count/count/remove_count
data remove storage midwut:main Inventory[-1]
execute if data storage midwut:main Inventory[-1] run function midwut:y_functions/items_and_storages/clear/inventory_by_count/count/main

