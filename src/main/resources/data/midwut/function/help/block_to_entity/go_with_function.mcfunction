# §6 En
## A block at the place where the function is executed with a center alignment will turn into a falling block with the function running after it
## §e Input data example:
### function midwut:help/block_to_entity/go_with_function {function: "namespace:name"}

# §6 Ru
## Блок на месте исполнения функции с выравниванием по центру превратится в падающий блок с вызванной после от него функцией
## §e Пример входных данных:
### function midwut:help/block_to_entity/go_with_function {function: "namespace:name"}

$execute unless block ~ ~ ~ #minecraft:air align xyz positioned ~0.5 ~ ~0.5 run function midwut:help/block_to_entity/convert_with_function {function:"$(function)"}
