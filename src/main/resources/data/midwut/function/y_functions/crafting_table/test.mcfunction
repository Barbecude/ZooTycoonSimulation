#> midwut:y_functions/crafting_table/test

execute unless function midwut:y_functions/crafting_table/structure_destroy run return run function midwut:y_functions/crafting_table/destroy/main with storage midwut:main macros
execute if entity @a[limit=1,distance=..10] run function midwut:y_functions/crafting_table/tick with storage midwut:main macros
