#> midwut:y_functions/air_at_run_function/stairs/main
execute if block ~ ~ ~ #minecraft:stairs[half=bottom] positioned ~ ~0.4999 ~ if entity @s[dx=0] run return run function midwut:y_functions/air_at_run_function/stairs/main_facing
execute if block ~ ~ ~ #minecraft:stairs[half=top] positioned ~ ~-0.5 ~ if entity @s[dx=0] run return run function midwut:y_functions/air_at_run_function/stairs/main_facing


