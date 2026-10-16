execute if entity @s[tag=phys_veh.wheel.steering] run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/unit_vector/fx_f

scoreboard players operation #x2 main_score = #fx main_score
scoreboard players operation #x2 main_score *= #left.x main_score
scoreboard players operation #x2 main_score /= #1000 const

scoreboard players operation #y2 main_score = #fx main_score
scoreboard players operation #y2 main_score *= #left.y main_score
scoreboard players operation #y2 main_score /= #1000 const

scoreboard players operation #z2 main_score = #fx main_score
scoreboard players operation #z2 main_score *= #left.z main_score
scoreboard players operation #z2 main_score /= #1000 const
