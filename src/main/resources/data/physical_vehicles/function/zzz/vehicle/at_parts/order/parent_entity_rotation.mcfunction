execute as @e[type=#physical_vehicles:refers_to_vehicle/all,tag=phys_veh.selected_part,predicate=physical_vehicles:same_scores/object_parent_id,limit=1,x=0] run function physical_vehicles:zzz/vehicle/at_parts/order/matrixa2_copy

execute store result storage midwut:main macros.x float 0.0001 run scoreboard players get #m02_ main_score
execute store result storage midwut:main macros.y float 0.0001 run scoreboard players get #m12_ main_score
execute store result storage midwut:main macros.z float 0.0001 run scoreboard players get #m22_ main_score
function physical_vehicles:zzz/vehicle/math/quaternions/macro_facing with storage midwut:main macros