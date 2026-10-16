execute unless predicate physical_vehicles:player/driver run return fail
execute on vehicle run scoreboard players operation #id_test main_score = @s phys_veh.id.vehicle

data modify storage midwut:phys_veh vehicle_dashboard set value []
data modify storage midwut:main macros.value set value ""
execute as @e[type=minecraft:text_display,tag=phys_veh.main,predicate=physical_vehicles:same_scores/vehicle_id,limit=1] run function physical_vehicles:zzz/vehicle/vehicle_dashboard/data

execute if data storage midwut:phys_veh vehicle_dashboard[0] run function physical_vehicles:zzz/vehicle/vehicle_dashboard/compile_pre
data modify storage midwut:main macros.vehicle_dashboard set from storage midwut:phys_veh vehicle_dashboard
data modify storage midwut:main macros.value set string storage midwut:main macros.value 1
execute store result score #temp main_score run data get storage midwut:main macros.value
execute if score #temp main_score matches 6.. run function physical_vehicles:zzz/player/triggers/vehicle_dashboard/dialog with storage midwut:main macros