data modify storage midwut:main temp set from entity @s data

scoreboard players operation #id_test main_score = @s phys_veh.id
#execute as @e[type=#physical_vehicles:refers_to_vehicle/all,tag=physical_vehicle,tag=!phys_veh.selected_part,tag=!phys_veh.main,distance=..80,predicate=physical_vehicles:same_scores/vehicle_id] run tag @s add phys_veh.selected_part

execute store result score #temp main_score if entity @e[type=llama,tag=physical_vehicle,distance=..80,nbt={ChestedHorse:1b},predicate=physical_vehicles:same_scores/vehicle_id,limit=1]

execute if score #temp main_score matches 1 as @e[type=llama,tag=physical_vehicle,distance=..80,nbt={ChestedHorse:1b},predicate=physical_vehicles:same_scores/vehicle_id] run function physical_vehicles:item/remover/llama-s_chests
kill
kill @e[type=#physical_vehicles:refers_to_vehicle/all,distance=..80,predicate=physical_vehicles:same_scores/vehicle_id]

