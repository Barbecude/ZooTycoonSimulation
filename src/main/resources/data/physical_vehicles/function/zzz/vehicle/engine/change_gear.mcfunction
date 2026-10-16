scoreboard players operation #gear_num main_score > @s phys_veh.gear_num_min
scoreboard players operation #gear_num main_score < @s phys_veh.gear_num_max
execute if entity @s[tag=phys_veh.doesnt_have_neutral_gear] if score #gear_num main_score matches 0 if score @s phys_veh.gear_num_max matches 1.. run scoreboard players set #gear_num main_score 1
execute if entity @s[tag=phys_veh.doesnt_have_neutral_gear] if score #gear_num main_score matches 0 if score @s phys_veh.gear_num_max matches ..-1 run scoreboard players set #gear_num main_score -1
scoreboard players operation @s phys_veh.gear_num = #gear_num main_score

execute store result storage midwut:main macros.gear_index int 1 run scoreboard players operation #gear_num main_score += @s phys_veh.gear_num_non_neg_index
function physical_vehicles:zzz/vehicle/engine/change_gear_get with storage midwut:main macros

