execute if score #rolling_in_air main_score matches 1.. if score @s phys_veh.spin.z matches 15000.. run return fail
execute if score #rolling_in_air main_score matches ..-1 if score @s phys_veh.spin.z matches ..-15000 run return fail

scoreboard players operation #rolling_in_air main_score *= @s phys_veh.rolling_in_air
scoreboard players operation @s phys_veh.spin.z += #rolling_in_air main_score
