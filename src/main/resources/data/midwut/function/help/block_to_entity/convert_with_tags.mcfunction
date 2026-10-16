# §6 En
## A block at the place where the function is executed will turn into a falling block with input tags
## §e Input data example:
### data modify storage midwut:main Tags set value ["custom_tag","custom_falling_block"]
### function midwut:help/block_to_entity/convert_with_tags

# §6 Ru
## Блок на месте исполнения функции превратится в падающий блок с входными тегами
## §e Пример входных данных:
### data modify storage midwut:main Tags set value ["custom_tag","custom_falling_block"]
### function midwut:help/block_to_entity/convert_with_tags

function midwut:help/get_block/blockstate
execute summon falling_block run function midwut:y_functions/block_to_entity/main_with_tags

