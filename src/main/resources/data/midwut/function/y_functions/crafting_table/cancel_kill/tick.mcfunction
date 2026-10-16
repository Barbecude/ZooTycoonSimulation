data modify storage midwut:main temp set from storage midwut:main midwut_crafting_tables
function midwut:y_functions/crafting_table/cancel_kill/loop

execute if data storage midwut:main midwut_crafting_tables[0] run schedule function midwut:y_functions/crafting_table/cancel_kill/tick 30s replace