#> midwut:help/inventory_modifier/add_append

# §6 Ru
## Эта функция применяет с добавлением новые слоты из "midwut:main Inventory" в "midwut:main PlayerInventory". Слоты из "midwut:main Inventory" применяются только в отсутствующие слоты из "midwut:main PlayerInventory"
## Входные данные: 
## data get storage midwut:main PlayerInventory — инвентарь игрока
## data get storage midwut:main Inventory — применяемый инвентарь

# §6 En
## This function applies with the addition of new slots from "midwut:main Inventory" to the player. Slots from "midwut:main Inventory" are applied only to missing slots from "midwut:main PlayerInventory"
## §e Input data:
## data get storage midwut:main PlayerInventory — player's inventory
## data get storage midwut:main Inventory — applicable inventory

#
data modify storage midwut:main inventory set value []
data modify storage midwut:main hotbar set value []

execute unless function midwut:y_functions/inv_mod_parts/add_merge_check run function midwut:y_functions/inv_mod_parts/add_append

execute in midwut:main positioned 0 0 0 run function midwut:y_functions/inv_mod_parts/moving_all_parts
