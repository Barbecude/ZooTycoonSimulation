execute unless predicate physical_vehicles:player/driver run return fail
$data modify storage midwut:main data set value $(data)
execute on vehicle run scoreboard players operation #id_test main_score = @s phys_veh.id.vehicle
execute as @e[type=minecraft:text_display,tag=phys_veh.main,predicate=physical_vehicles:same_scores/vehicle_id,limit=1] run function physical_vehicles:zzz/vehicle/vehicle_dashboard/set_data
