# §6 En
## A block at the place where the function is executed will turn into a falling block with the function running after it
## §e Input data example:
### function midwut:help/block_to_entity/convert_with_function {function: "namespace:name"}

# §6 Ru
## Блок на месте исполнения функции превратится в падающий блок с вызванной после от него функцией
## §e Пример входных данных:
### function midwut:help/block_to_entity/convert_with_function {function: "namespace:name"}

function midwut:help/get_block/blockstate
$execute summon falling_block run function midwut:y_functions/block_to_entity/main_with_function {function:"$(function)"}

