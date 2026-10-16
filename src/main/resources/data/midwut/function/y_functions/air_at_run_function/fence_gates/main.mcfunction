execute if block ~ ~ ~ #fence_gates[open=true] run return 1
execute if block ~ ~ ~ #fence_gates[in_wall=false] run return run function midwut:y_functions/air_at_run_function/fence_gates/no_in_wall
execute if block ~ ~ ~ #fence_gates[in_wall=true] run return run function midwut:y_functions/air_at_run_function/fence_gates/in_wall
return 1