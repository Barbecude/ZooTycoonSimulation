execute unless score @s phys_veh.id.object_parent = @s phys_veh.id.object_parent run return run function physical_vehicles:zzz/vehicle/at_parts/order/rotation_main_data
execute as @e[type=#physical_vehicles:refers_to_vehicle/all,tag=phys_veh.selected_part,predicate=physical_vehicles:same_scores/object_parent_id,limit=1,x=0] run function physical_vehicles:zzz/vehicle/at_parts/order/rotation_parent_data
scoreboard players operation @s phys_veh.m00_3 = #m00_3_ main_score
scoreboard players operation @s phys_veh.m01_3 = #m01_3_ main_score
scoreboard players operation @s phys_veh.m02_3 = #m02_3_ main_score
scoreboard players operation @s phys_veh.m10_3 = #m10_3_ main_score
scoreboard players operation @s phys_veh.m11_3 = #m11_3_ main_score
scoreboard players operation @s phys_veh.m12_3 = #m12_3_ main_score
scoreboard players operation @s phys_veh.m20_3 = #m20_3_ main_score
scoreboard players operation @s phys_veh.m21_3 = #m21_3_ main_score
scoreboard players operation @s phys_veh.m22_3 = #m22_3_ main_score
