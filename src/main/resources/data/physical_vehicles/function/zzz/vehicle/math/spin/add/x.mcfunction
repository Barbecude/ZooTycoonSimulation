function physical_vehicles:zzz/vehicle/math/spin/decrease/x
execute if score @s phys_veh.spin.x matches -20..20 run return run scoreboard players set @s phys_veh.spin.x 0

scoreboard players operation #temp main_score = @s phys_veh.spin.x
scoreboard players operation #temp main_score /= #tick_rate main_score
scoreboard players operation @s phys_veh.add_angle.x += #temp main_score