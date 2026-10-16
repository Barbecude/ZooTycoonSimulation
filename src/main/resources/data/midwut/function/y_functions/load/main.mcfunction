execute in midwut:main positioned 0.0 0.0 0.0 run function midwut:y_functions/load/end

function #midwut:if_loaded/load
execute if data storage midwut:main midwut_crafting_tables[0] run schedule function midwut:tick_crafting_table 1t replace
execute if data storage midwut:main midwut_crafting_tables[0] run schedule function midwut:y_functions/crafting_table/cancel_kill/tick 30s replace
