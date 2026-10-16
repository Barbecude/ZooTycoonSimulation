scoreboard players operation #v main_score = @s phys_veh.offset2.z
scoreboard players operation #v main_score *= @s phys_veh.strength
scoreboard players operation #d main_score = @s phys_veh.velocity.z
scoreboard players operation #d main_score *= @s phys_veh.damping

scoreboard players operation #v main_score += #d main_score
scoreboard players operation #v main_score *= #tick_rate_d4 main_score
scoreboard players operation #v main_score /= @s phys_veh.mass
scoreboard players operation @s phys_veh.velocity.z -= #v main_score

scoreboard players operation #temp main_score = @s phys_veh.velocity.z
scoreboard players operation #temp main_score /= #tick_rate main_score
scoreboard players operation @s phys_veh.offset2.z += #temp main_score

execute if score @s phys_veh.offset2.z matches 0.. run return run function physical_vehicles:zzz/vehicle/at_parts/cannon/recoil_stop
execute if score @s phys_veh.offset2.z < @s phys_veh.max_delta run function physical_vehicles:zzz/vehicle/at_parts/cannon/recoil_lenght