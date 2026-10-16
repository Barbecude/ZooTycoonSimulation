function midwut:y_functions/crafting_table/cancel_kill/pos with storage midwut:main temp[-1]

data remove storage midwut:main temp[-1]
execute if data storage midwut:main temp[0] run function midwut:y_functions/crafting_table/cancel_kill/loop