scoreboard players operation #fy_w main_score /= @s phys_veh.mass
scoreboard players operation #temp main_score = @s phys_veh.radius
scoreboard players operation #temp main_score /= #10 const
scoreboard players operation #temp main_score *= #fy_w main_score
scoreboard players operation #temp main_score /= #tick_rate main_score
scoreboard players operation #temp main_score /= @s phys_veh.moment_of_inertia_no_mass.x
scoreboard players operation @s phys_veh.spin.x -= #temp main_score