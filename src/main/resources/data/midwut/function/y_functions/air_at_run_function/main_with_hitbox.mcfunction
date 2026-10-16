#> midwut:y_functions/air_at_run_function/main_with_hitbox
## execute positioned ~ ~-0.0001 ~ if entity @s[dx=0] positioned ~ ~ ~ if entity @s[dx=0]

execute if block ~ ~ ~ #midwut:pixels/custom_collision align xyz run return run function #midwut:air_at_run_function/custom_collision

execute if block ~ ~ ~ #midwut:pixels/with_hitbox align xyz run return run function midwut:y_functions/air_at_run_function/with_hitbox

return run function midwut:y_functions/air_at_run_function/main_only_collision
