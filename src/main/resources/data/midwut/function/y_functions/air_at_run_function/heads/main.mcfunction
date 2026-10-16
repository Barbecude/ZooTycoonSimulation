execute if block ~ ~ ~ #midwut:heads/normal positioned ~0.25 ~-0.0001 ~0.25 if entity @s[dx=0] positioned ~-0.5 ~-0.4999 ~-0.5 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ #midwut:heads/wall run return run function midwut:y_functions/air_at_run_function/heads/wall
return 1

