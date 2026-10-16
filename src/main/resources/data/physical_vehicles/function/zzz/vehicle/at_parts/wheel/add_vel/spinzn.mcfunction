scoreboard players operation #z2 main_score /= #tick_rate- main_score
scoreboard players operation #z2 main_score /= @s phys_veh.moment_of_inertia_no_mass.z
scoreboard players operation @s phys_veh.spin.z -= #z2 main_score