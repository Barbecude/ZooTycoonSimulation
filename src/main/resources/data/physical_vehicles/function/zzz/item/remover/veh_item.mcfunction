scoreboard players operation #id_test main_score = @s phys_veh.id.vehicle

execute as @e[type=minecraft:llama,tag=physical_vehicle,distance=..80,nbt={ChestedHorse:1b},predicate=physical_vehicles:same_scores/vehicle_id] run function physical_vehicles:zzz/item/remover/llama-s_chests
kill @e[type=#physical_vehicles:refers_to_vehicle/all,distance=..80,predicate=physical_vehicles:same_scores/vehicle_id]

execute if entity @s[tag=phys_veh.summoned_vehicle] run return run kill @s

data modify storage midwut:main data set from entity @s data
kill @s

summon minecraft:item ~ ~ ~ {Item:{id:"minecraft:dolphin_spawn_egg",components:{"minecraft:entity_data":{data:{phys_veh:{}},Tags:["phys_veh.spawn"],id:"minecraft:pig",Silent:true,NoAI:true,Invulnerable:true,DeathLootTable:""}}},Tags:["get_id"]}
execute as @e[type=minecraft:item,tag=get_id,limit=1,distance=..0.1] run function physical_vehicles:zzz/item/remover/veh_item_data


