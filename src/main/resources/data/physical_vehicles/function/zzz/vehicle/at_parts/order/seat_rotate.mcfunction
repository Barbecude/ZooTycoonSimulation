execute if entity @s[tag=phys_veh.seat_has_add_rotation] on vehicle rotated as @s on passengers run return run function physical_vehicles:zzz/vehicle/at_parts/seat_add_rotation with entity @s data.phys_veh
execute on vehicle rotated as @s on passengers run rotate @s ~ ~
