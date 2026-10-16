#> midwut:y_functions/air_at_run_function/main_only_collision

## execute positioned ~ ~-0.01 ~ if entity @s[dy=0.1] positioned ~ ~0.01 ~ if entity @s[dx=0]

execute if block ~ ~ ~ #midwut:pixels/custom_collision align xyz run return run function #midwut:air_at_run_function/custom_collision

execute if block ~ ~ ~ #midwut:pixels/hard_hitbox run return run function midwut:y_functions/air_at_run_function/hard_hitbox

execute if block ~ ~ ~ #midwut:pixels/constant/1 run return run execute align xyz positioned ~-0.01 ~0.0624 ~-0.01 if entity @s[dx=0.1,dz=0.1]
execute if block ~ ~ ~ #midwut:pixels/constant/2 run return run execute align xyz positioned ~-0.01 ~0.1249 ~-0.01 if entity @s[dx=0.1,dz=0.1]
#execute if block ~ ~ ~ #midwut:pixels/constant/3 run return run execute align xyz positioned ~-0.01 ~0.1874 ~-0.01 if entity @s[dx=0.1,dz=0.1]
#execute if block ~ ~ ~ #midwut:pixels/constant/4 run return run execute align xyz positioned ~-0.01 ~0.2499 ~-0.01 if entity @s[dx=0.1,dz=0.1]
#execute if block ~ ~ ~ #midwut:pixels/constant/5 run return run execute align xyz positioned ~-0.01 ~0.3124 ~-0.01 if entity @s[dx=0.1,dz=0.1]
execute if block ~ ~ ~ #midwut:pixels/constant/6 run return run execute align xyz positioned ~-0.01 ~0.3749 ~-0.01 if entity @s[dx=0.1,dz=0.1]
execute if block ~ ~ ~ #midwut:pixels/constant/7 run return run execute align xyz positioned ~-0.01 ~0.4374 ~-0.01 if entity @s[dx=0.1,dz=0.1]
execute if block ~ ~ ~ #midwut:pixels/constant/8 run return run execute align xyz positioned ~-0.01 ~0.4999 ~-0.01 if entity @s[dx=0.1,dz=0.1]
execute if block ~ ~ ~ #midwut:pixels/constant/9 run return run execute align xyz positioned ~-0.01 ~0.5624 ~-0.01 if entity @s[dx=0.1,dz=0.1]
#execute if block ~ ~ ~ #midwut:pixels/constant/10 run return run execute align xyz positioned ~-0.01 ~0.6249 ~-0.01 if entity @s[dx=0.1,dz=0.1]
#execute if block ~ ~ ~ #midwut:pixels/constant/11 run return run execute align xyz positioned ~-0.01 ~0.6874 ~-0.01 if entity @s[dx=0.1,dz=0.1]
execute if block ~ ~ ~ #midwut:pixels/constant/12 run return run execute align xyz positioned ~-0.01 ~0.7499 ~-0.01 if entity @s[dx=0.1,dz=0.1]
#execute if block ~ ~ ~ #midwut:pixels/constant/13 run return run execute align xyz positioned ~-0.01 ~0.8124 ~-0.01 if entity @s[dx=0.1,dz=0.1]
execute if block ~ ~ ~ #midwut:pixels/constant/14 run return run execute align xyz positioned ~-0.01 ~0.8749 ~-0.01 if entity @s[dx=0.1,dz=0.1]
execute if block ~ ~ ~ #midwut:pixels/constant/15 run return run execute align xyz positioned ~-0.01 ~0.9374 ~-0.01 if entity @s[dx=0.1,dz=0.1]
execute if block ~ ~ ~ minecraft:lily_pad align xyz run return run function midwut:y_functions/air_at_run_function/lily_pad
execute if block ~ ~ ~ minecraft:composter run return run execute align xyz positioned ~0.125 ~0.1249 ~0.125 if entity @s[dx=0] positioned ~-0.25 ~ ~-0.25 if entity @s[dx=0]
execute if block ~ ~ ~ minecraft:conduit align xyz run return run function midwut:y_functions/air_at_run_function/conduit
execute if block ~ ~ ~ #minecraft:flower_pots align xyz run return run function midwut:y_functions/air_at_run_function/flower_pots
execute if block ~ ~ ~ minecraft:sniffer_egg align xyz run return run function midwut:y_functions/air_at_run_function/sniffer_egg
execute if block ~ ~ ~ minecraft:dragon_egg align xyz run return run function midwut:y_functions/air_at_run_function/dragon_egg


