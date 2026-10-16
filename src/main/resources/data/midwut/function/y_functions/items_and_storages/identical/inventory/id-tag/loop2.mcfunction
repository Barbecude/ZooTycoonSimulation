execute store success score #midwut.out main_score run data modify storage midwut:main Items2[-1] set from storage midwut:main Inventory[-1]

data remove storage midwut:main Items2[-1]
execute if score #midwut.out main_score matches 1 if data storage midwut:main Items2[0] run function midwut:y_functions/items_and_storages/identical/inventory/id-tag/loop2