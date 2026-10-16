execute if score #x2 main_score matches ..-1 run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/add_vel/spinxn
scoreboard players operation #x2 main_score /= @s phys_veh.moment_of_inertia_no_mass.x
scoreboard players operation #x2 main_score /= #tick_rate main_score
scoreboard players operation @s phys_veh.spin.x += #x2 main_score