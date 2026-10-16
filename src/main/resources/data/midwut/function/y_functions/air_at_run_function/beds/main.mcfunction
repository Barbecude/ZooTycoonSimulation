execute positioned ~ ~0.1875 ~ if entity @s[dx=0] positioned ~ ~-0.625 ~ if entity @s[dx=0] run return 0

execute if block ~ ~ ~ #minecraft:beds[part=foot] run return run function midwut:y_functions/air_at_run_function/beds/foot
execute if block ~ ~ ~ #minecraft:beds[part=head] run return run function midwut:y_functions/air_at_run_function/beds/head
