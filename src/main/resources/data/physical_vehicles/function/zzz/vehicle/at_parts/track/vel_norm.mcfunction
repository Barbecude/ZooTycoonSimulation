scoreboard players operation #temp main_score = @s phys_veh.velocity.z
scoreboard players operation #temp main_score /= @s phys_veh.max_delta
scoreboard players operation @s phys_veh.gear_num += #temp main_score
scoreboard players operation #temp main_score *= @s phys_veh.max_delta
scoreboard players operation @s phys_veh.velocity.z -= #temp main_score