tag @s add phys_veh.wheel.on_ground
execute positioned ^ ^ ^-0.003 run function physical_vehicles:zzz/vehicle/at_parts/wheel/on_ground_functions

execute positioned ~ ~-0.003 ~ unless function midwut:help/check/air_with_hitbox run return run scoreboard players set #norm.y main_score 1000
execute positioned ~ ~0.003 ~ unless function midwut:help/check/air_with_hitbox run return run scoreboard players set #norm.y main_score -1000
execute positioned ~-0.003 ~ ~ unless function midwut:help/check/air_with_hitbox run return run scoreboard players set #norm.x main_score 1000
execute positioned ~0.003 ~ ~ unless function midwut:help/check/air_with_hitbox run return run scoreboard players set #norm.x main_score -1000
execute positioned ~ ~ ~-0.003 unless function midwut:help/check/air_with_hitbox run return run scoreboard players set #norm.z main_score 1000
execute positioned ~ ~ ~0.003 unless function midwut:help/check/air_with_hitbox run return run scoreboard players set #norm.z main_score -1000
