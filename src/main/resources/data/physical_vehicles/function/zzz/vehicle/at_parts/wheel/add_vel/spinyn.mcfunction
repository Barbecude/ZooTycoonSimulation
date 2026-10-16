scoreboard players operation #y2 main_score /= #tick_rate- main_score
scoreboard players operation #y2 main_score /= @s phys_veh.moment_of_inertia_no_mass.y
scoreboard players operation @s phys_veh.spin.y -= #y2 main_score