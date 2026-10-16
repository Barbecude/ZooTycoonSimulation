scoreboard players operation @s phys_veh.offset_saved.x = @s phys_veh.offset.x
scoreboard players operation @s phys_veh.offset_saved.y = @s phys_veh.offset.y
scoreboard players operation @s phys_veh.offset_saved.z = @s phys_veh.offset.z
scoreboard players operation @s phys_veh.offset.x = #x main_score
scoreboard players operation @s phys_veh.offset.y = #y main_score
scoreboard players operation @s phys_veh.offset.z = #z main_score
execute on passengers on passengers if entity @s[type=minecraft:player] run function physical_vehicles:zzz/vehicle/at_parts/aiming/enable_player with storage midwut:main macros