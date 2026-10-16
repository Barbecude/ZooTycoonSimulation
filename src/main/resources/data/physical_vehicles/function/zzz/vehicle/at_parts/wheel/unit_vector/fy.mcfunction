execute if entity @s[tag=phys_veh.wheel.steering] run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/unit_vector/fy_f

scoreboard players operation #x3 main_score = #fy main_score
scoreboard players operation #x3 main_score *= #forward.x main_score
scoreboard players operation #x3 main_score /= #1000 const

scoreboard players operation #y3 main_score = #fy main_score
scoreboard players operation #y3 main_score *= #forward.y main_score
scoreboard players operation #y3 main_score /= #1000 const

scoreboard players operation #z3 main_score = #fy main_score
scoreboard players operation #z3 main_score *= #forward.z main_score
scoreboard players operation #z3 main_score /= #1000 const
