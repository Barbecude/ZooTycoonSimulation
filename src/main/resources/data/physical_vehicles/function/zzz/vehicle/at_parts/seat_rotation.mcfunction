#execute unless function physical_vehicles:zzz/vehicle/player_rotate/check on passengers rotated as @s run function physical_vehicles:zzz/vehicle/player_rotate/pre
execute if entity @s[tag=phys_veh.seat_has_add_rotation] run return run function physical_vehicles:zzz/vehicle/at_parts/seat_add_rotation with entity @s data.phys_veh
rotate @s ~ ~
