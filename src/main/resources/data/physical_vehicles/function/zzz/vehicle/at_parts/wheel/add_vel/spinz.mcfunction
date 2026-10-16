execute if score #z2 main_score matches ..-1 run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/add_vel/spinzn
scoreboard players operation #z2 main_score /= @s phys_veh.moment_of_inertia_no_mass.z
scoreboard players operation #z2 main_score /= #tick_rate main_score
scoreboard players operation @s phys_veh.spin.z += #z2 main_score