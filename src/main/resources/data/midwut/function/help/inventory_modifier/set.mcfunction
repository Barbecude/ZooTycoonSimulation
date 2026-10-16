#> midwut:help/inventory_modifier/set

# §6 Ru
## Эта функция полностью применяет "midwut:main Inventory" в игрока. Если слот в "midwut:main Inventory" не указан, то у игрока этот слот очистится
## §e Входные данные: 
## data get storage midwut:main Inventory — применяемый инвентарь

# §6 En
## This function fully applies "midwut:main Inventory" to the player. If the slot in "midwut:main Inventory" is not specified, then this slot will be cleared for the player
## §e Input data:
## data get storage midwut:main Inventory — applicable inventory

# 
data modify storage midwut:main inventory set value []
data modify storage midwut:main hotbar set value []

execute if data storage midwut:main Inventory[0] run function midwut:y_functions/inv_mod_parts/set

execute in midwut:main positioned 0 0 0 run function midwut:y_functions/inv_mod_parts/moving_all_parts
