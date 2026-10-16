scoreboard players operation @s phys_veh.offset.x = @s phys_veh.offset_saved.x
scoreboard players operation @s phys_veh.offset.y = @s phys_veh.offset_saved.y
scoreboard players operation @s phys_veh.offset.z = @s phys_veh.offset_saved.z

execute on passengers on passengers if entity @s[type=minecraft:player] run function physical_vehicles:zzz/vehicle/at_parts/aiming/disable_player