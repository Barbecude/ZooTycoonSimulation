#> midwut:help/get_block/blockstate

# §6 En
## Returns a block to the storage at the place of execution of the function
## §e Output data:
### data get storage midwut:main output_block.block

# §6 Ru
## Возвращает в сторейдж блок на месте исполнения функции в формате дисплея
## §e Выходные данные:
### data get storage midwut:main output_block.block

# Main
execute if block ~ ~ ~ #minecraft:air run return run function midwut:y_functions/get_block/air
clone ~ ~ ~ ~ ~ ~ to midwut:main 2 0 0 strict replace normal
execute in midwut:main run function midwut:y_functions/get_block/blockstate/main

