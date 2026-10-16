#> midwut:help/get_block/properties_converter

# §6 En
## This function converts Properties from the block state format to the format for the setblock command.
## §e Input data:
### data get storage midwut:main Properties
## §e Output data:
### data get storage midwut:main blockstates

# §6 Ru
## Конвертирует Properties из блок стейт формата, в формат для команды setblock
## §e Входные данные:
### data get storage midwut:main Properties
## §e Выходные данные:
### data get storage midwut:main blockstates

# Main

function midwut:y_functions/get_block/properties_converter/string with storage midwut:main
scoreboard players set #i main_score 0
scoreboard players set #i1 main_score 1
data modify storage midwut:main macros.i set value 0
data modify storage midwut:main macros.i1 set value 1
execute store result score #len main_score run data get storage midwut:main blockstates
function midwut:y_functions/get_block/properties_converter/loop
function midwut:y_functions/get_block/properties_converter/sq with storage midwut:main
