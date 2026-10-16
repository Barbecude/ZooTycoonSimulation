tag @s remove phys_veh.dynamic_collision_check
execute as @e[type=minecraft:text_display,tag=phys_veh.main,tag=!phys_veh.kinetic,tag=phys_veh.collision_in_radius,tag=!phys_veh.selected_part,distance=..96] run function physical_vehicles:zzz/vehicle/collision/dynamic/add_end
execute if score @s phys_veh.coll_count matches 1.. run function physical_vehicles:zzz/vehicle/collision/dynamic/apply_vels