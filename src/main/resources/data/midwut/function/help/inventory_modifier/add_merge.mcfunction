#> midwut:help/inventory_modifier/add_merge

# §6 Ru
## Эта функция применяет с заменой новые слоты из "midwut:main Inventory" в "midwut:main PlayerInventory". Если слот в "midwut:main Inventory" не указан, то будет сохранён слот игрока из "midwut:main PlayerInventory"
## Входные данные: 
## data get storage midwut:main PlayerInventory — инвентарь игрока
## data get storage midwut:main Inventory — применяемый инвентарь

# §6 En
## This function applies with replacement new slots from "midwut:main Inventory" to the player. If the slot in "midwut:main Inventory" is not specified, the player slot from "midwut:main PlayerInventory" will be saved
## §e Input data:
## data get storage midwut:main PlayerInventory — player's inventory
## data get storage midwut:main Inventory — applicable inventory

# 
data modify storage midwut:main inventory set value []
data modify storage midwut:main hotbar set value []

execute unless function midwut:y_functions/inv_mod_parts/add_merge_check run function midwut:y_functions/inv_mod_parts/add_merge

execute in midwut:main positioned 0 0 0 run function midwut:y_functions/inv_mod_parts/moving_all_parts
