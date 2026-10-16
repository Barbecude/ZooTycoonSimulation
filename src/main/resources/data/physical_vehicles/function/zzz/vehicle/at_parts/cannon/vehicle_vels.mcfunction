tag @s add phys_veh.step_ignore

scoreboard players operation #x2 main_score /= @s phys_veh.moment_of_inertia_no_mass.x
scoreboard players operation #y2 main_score /= @s phys_veh.moment_of_inertia_no_mass.y
scoreboard players operation #z2 main_score /= @s phys_veh.moment_of_inertia_no_mass.z

scoreboard players operation @s phys_veh.velocity.x += #vx main_score
scoreboard players operation @s phys_veh.velocity.y += #vy main_score
scoreboard players operation @s phys_veh.velocity.z += #vz main_score
scoreboard players operation @s phys_veh.spin.x += #x2 main_score
scoreboard players operation @s phys_veh.spin.y += #y2 main_score
scoreboard players operation @s phys_veh.spin.z += #z2 main_score