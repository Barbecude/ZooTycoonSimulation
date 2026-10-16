execute if entity @s[tag=phys_veh.debug.back_last_location] run say BACK
tag @s remove phys_veh.back_last_location
scoreboard players operation #main.pos.x main_score = @s phys_veh.last_pos.x
scoreboard players operation #main.pos.y main_score = @s phys_veh.last_pos.y
scoreboard players operation #main.pos.z main_score = @s phys_veh.last_pos.z
scoreboard players operation @s phys_veh.quaternion.x = @s phys_veh.last_quaternion.x
scoreboard players operation @s phys_veh.quaternion.y = @s phys_veh.last_quaternion.y
scoreboard players operation @s phys_veh.quaternion.z = @s phys_veh.last_quaternion.z
scoreboard players operation @s phys_veh.quaternion.w = @s phys_veh.last_quaternion.w
function physical_vehicles:zzz/vehicle/math/quaternions/to_matrix
