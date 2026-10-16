function physical_vehicles:zzz/vehicle/math/spin/decrease/y
execute if score @s phys_veh.spin.y matches -20..20 run return run scoreboard players set @s phys_veh.spin.y 0

scoreboard players operation #temp main_score = @s phys_veh.spin.y
scoreboard players operation #temp main_score /= #tick_rate main_score
scoreboard players operation @s phys_veh.add_angle.y += #temp main_score