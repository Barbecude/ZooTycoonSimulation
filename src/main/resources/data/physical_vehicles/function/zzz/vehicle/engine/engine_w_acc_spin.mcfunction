scoreboard players operation #wngine_spin_to_vehicle main_score = #engine_w_acc main_score
scoreboard players operation #wngine_spin_to_vehicle main_score *= #1000 const
scoreboard players operation #wngine_spin_to_vehicle main_score /= @s phys_veh.moment_of_inertia.z
scoreboard players operation #wngine_spin_to_vehicle main_score /= #tick_rate_0 main_score
scoreboard players operation @s phys_veh.spin.z += #wngine_spin_to_vehicle main_score