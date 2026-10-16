execute if score #gravity main_score matches ..-1 run return run function physical_vehicles:zzz/shell/gravity_neg
scoreboard players operation #gravity main_score /= #111 const
scoreboard players operation @s phys_veh.gravity.y += #gravity main_score
