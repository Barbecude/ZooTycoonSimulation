
execute if entity @s[tag=phys_veh.transaltion_offset] run function physical_vehicles:zzz/vehicle/at_parts/transaltion_offset
execute if entity @s[tag=!phys_veh.disable.offset] if score @s phys_veh.id.object_parent = @s phys_veh.id.object_parent run return run function physical_vehicles:zzz/vehicle/at_parts/order/offset
execute if entity @s[tag=!phys_veh.disable.offset] run function physical_vehicles:zzz/vehicle/at_parts/offset