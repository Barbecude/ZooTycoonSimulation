# §6 En
## A block at the place where the function is executed with a center alignment will turn into a falling block with input tags
## §e Input data example:
### data modify storage midwut:main Tags set value ["custom_tag","custom_falling_block"]
### function midwut:help/block_to_entity/go_with_tags

# §6 Ru
## Блок на месте исполнения функции с выравниванием по центру превратится в падающий блок с входными тегами
## §e Пример входных данных:
### data modify storage midwut:main Tags set value ["custom_tag","custom_falling_block"]
### function midwut:help/block_to_entity/go_with_tags

execute unless block ~ ~ ~ #minecraft:air align xyz positioned ~0.5 ~ ~0.5 run function midwut:help/block_to_entity/convert_with_tags
