execute if function midwut:y_functions/get_block/properties_converter/check_char_pre run function midwut:y_functions/get_block/properties_converter/found with storage midwut:main macros

execute store result storage midwut:main macros.i int 1 run scoreboard players add #i main_score 1
execute if score #i main_score >= #len main_score run return 1
execute store result storage midwut:main macros.i1 int 1 run scoreboard players add #i1 main_score 1
function midwut:y_functions/get_block/properties_converter/loop