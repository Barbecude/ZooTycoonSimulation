execute if score #y2 main_score matches ..-1 run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/add_vel/spinyn
scoreboard players operation #y2 main_score /= @s phys_veh.moment_of_inertia_no_mass.y
scoreboard players operation #y2 main_score /= #tick_rate main_score
scoreboard players operation @s phys_veh.spin.y += #y2 main_score