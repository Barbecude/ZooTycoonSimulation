scoreboard players operation #temp main_score = @s phys_veh.spin.x
scoreboard players operation #temp main_score *= @s phys_veh.radius
scoreboard players operation #temp main_score /= #10000 const
scoreboard players operation #temp main_score /= #tick_rate main_score
scoreboard players operation @s phys_veh.velocity.z += #temp main_score
execute if score @s phys_veh.velocity.z >= @s phys_veh.max_delta run function physical_vehicles:zzz/vehicle/at_parts/track/add_index
execute if score @s phys_veh.velocity.z <= @s phys_veh.max_delta_neg run function physical_vehicles:zzz/vehicle/at_parts/track/add_index_inv
#execute if entity @s[tag=phys_veh.track.have_sound] run function physical_vehicles:zzz/vehicle/at_parts/track/sound_check
