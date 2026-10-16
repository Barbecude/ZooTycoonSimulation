execute if entity @s[tag=phys_veh.wheel.steering] run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/unit_vector/vl_f

scoreboard players operation #vl.x main_score *= #left.x main_score
scoreboard players operation #vl.x main_score /= #1000 const

scoreboard players operation #vl.y main_score *= #left.y main_score
scoreboard players operation #vl.y main_score /= #1000 const

scoreboard players operation #vl.z main_score *= #left.z main_score
scoreboard players operation #vl.z main_score /= #1000 const
