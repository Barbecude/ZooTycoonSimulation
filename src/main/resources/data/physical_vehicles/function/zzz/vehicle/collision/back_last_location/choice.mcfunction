execute if entity @s[tag=phys_veh.back_last_location] run return run function physical_vehicles:zzz/vehicle/collision/back_last_location/end
scoreboard players operation @s phys_veh.last_pos.x = #main.pos.x main_score
scoreboard players operation @s phys_veh.last_pos.y = #main.pos.y main_score
scoreboard players operation @s phys_veh.last_pos.z = #main.pos.z main_score
scoreboard players operation @s phys_veh.last_quaternion.x = @s phys_veh.quaternion.x
scoreboard players operation @s phys_veh.last_quaternion.y = @s phys_veh.quaternion.y
scoreboard players operation @s phys_veh.last_quaternion.z = @s phys_veh.quaternion.z
scoreboard players operation @s phys_veh.last_quaternion.w = @s phys_veh.quaternion.w