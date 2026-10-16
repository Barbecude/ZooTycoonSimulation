function physical_vehicles:zzz/vehicle/math/spin/decrease/z
execute if score @s phys_veh.spin.z matches -20..20 run return run scoreboard players set @s phys_veh.spin.z 0

scoreboard players operation #temp main_score = @s phys_veh.spin.z
scoreboard players operation #temp main_score /= #tick_rate main_score
scoreboard players operation @s phys_veh.add_angle.z += #temp main_score