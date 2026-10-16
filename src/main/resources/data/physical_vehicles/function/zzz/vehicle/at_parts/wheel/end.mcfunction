#> physical_vehicles:zzz/vehicle/at_parts/wheel/end
execute if score #phys_veh.mob_griefing main_score matches 1 if block ~ ~ ~ #midwut:natura/all run setblock ~ ~ ~ air destroy

execute positioned ^ ^ ^-0.003 unless function midwut:help/check/air_with_hitbox positioned ^ ^ ^0.003 run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/on_ground_get
scoreboard players set #distance main_score 0
