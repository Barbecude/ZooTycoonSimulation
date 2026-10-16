#> midwut:help/items_and_storages/clear/list/matches_completely

# §6 Ru
## Эта функция удалит все элементы в `storage midwut:main Inventory`, присутствующие и полностью совпадающие в `storage midwut:main Items`.
## §e Входные данные: 
## data get storage midwut:main Inventory — массив, хранящий какие-то элементы.
## data get storage midwut:main Items — массив, элементы, которые надо удалить.
## §e Выходные данные: 
## data get storage midwut:main Inventory — массив, хранящий не удалённые элементы.

# §6 En
## This function will remove all items in `storage midwut:main Inventory` that are present and completely match in `storage midwut:main Items`.
## §e Input data: 
## data get storage midwut:main Inventory — an array storing some elements.
## data get storage midwut:main Items — array, elements to be deleted.
## §e Output data: 
## data get storage midwut:main Inventory — an array that stores items that have not been deleted.

# 
scoreboard players set #midwut.out main_score 0
execute if data storage midwut:main Inventory[0] if data storage midwut:main Items[0] run function midwut:y_functions/items_and_storages/clear/list/matches_completely/pre
return run scoreboard players get #midwut.out main_score
