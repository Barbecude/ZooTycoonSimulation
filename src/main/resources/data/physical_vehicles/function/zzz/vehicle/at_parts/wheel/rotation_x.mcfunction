scoreboard players operation #temp main_score = @s phys_veh.spin.x
scoreboard players operation #temp main_score /= #tick_rate main_score
scoreboard players operation @s phys_veh.add_angle.x += #temp main_score

execute if entity @s[tag=!phys_veh.objmc_model] run function physical_vehicles:zzz/vehicle/at_parts/wheel/rotation_x_set


