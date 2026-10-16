scoreboard players operation #d main_score *= #e main_score
scoreboard players operation #d main_score /= #100 const

scoreboard players remove #a main_score 1
execute if score #a main_score matches 2.. run function physical_vehicles:zzz/vehicle/engine/engine_torque_loop