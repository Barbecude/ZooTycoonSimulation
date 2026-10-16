execute if entity @s[tag=phys_veh.wheel.steering] run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/unit_vector/vf_f

scoreboard players operation #vf.x main_score *= #forward.x main_score
scoreboard players operation #vf.x main_score /= #1000 const

scoreboard players operation #vf.y main_score *= #forward.y main_score
scoreboard players operation #vf.y main_score /= #1000 const

scoreboard players operation #vf.z main_score *= #forward.z main_score
scoreboard players operation #vf.z main_score /= #1000 const
