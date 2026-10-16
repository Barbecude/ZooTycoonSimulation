tag @s remove phys_veh.collision_in_radius
execute if score @s phys_veh.coll_count matches 1.. run function physical_vehicles:zzz/vehicle/collision/dynamic/apply_vels