#> midwut:y_functions/items_and_storages/clear/inventory_by_count/tag/pre
data modify storage midwut:main Inventory2 set value []
data modify storage midwut:main Items2 set from storage midwut:main Items
function midwut:y_functions/items_and_storages/clear/inventory_by_count/id-tag/main
data modify storage midwut:main Inventory set from storage midwut:main Inventory2
