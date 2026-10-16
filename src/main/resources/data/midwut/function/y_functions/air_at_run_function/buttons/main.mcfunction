execute if block ~ ~ ~ #minecraft:buttons[powered=true] run return run function midwut:y_functions/air_at_run_function/buttons/true/main
execute if block ~ ~ ~ #minecraft:buttons[face=floor] run return run function midwut:y_functions/air_at_run_function/buttons/false/floor
execute if block ~ ~ ~ #minecraft:buttons[face=wall] run return run function midwut:y_functions/air_at_run_function/buttons/false/wall
execute if block ~ ~ ~ #minecraft:buttons[face=ceiling] run return run function midwut:y_functions/air_at_run_function/buttons/false/ceiling

