execute store result entity @s data.phys_veh.id.vehicle int 1 run scoreboard players get #physical_vehicles.id.vehicle main_score
execute store result entity @s data.phys_veh.id.seat int 1 run scoreboard players get #physical_vehicles.id.seat main_score

execute on vehicle run scoreboard players operation @s phys_veh.id.seat = #physical_vehicles.id.seat main_score
scoreboard players operation @s phys_veh.id.seat = #physical_vehicles.id.seat main_score
scoreboard players add #physical_vehicles.id.seat main_score 1

team join no_coll @s

execute if entity @s[tag=phys_veh.llama_seat_dr] if score #phys_veh.spawn.is_engine_started main_score matches 1 run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/engine_start_llama

execute if data storage midwut:main Data2.data.phys_veh.add_rotation run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/llama_seat_add_rotation
execute if data storage midwut:main Data2.data.phys_veh.improvements run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/llama_seat_improvements

execute if data storage midwut:main Data2.data.phys_veh.object_id store result score @s phys_veh.id.object run data get storage midwut:main Data2.data.phys_veh.object_id
execute if data storage midwut:main Data2.data.phys_veh.object_parent_id store result score @s phys_veh.id.object_parent run data get storage midwut:main Data2.data.phys_veh.object_parent_id
execute if data storage midwut:main Data2.data.phys_veh.order_id store result score @s phys_veh.id.order run data get storage midwut:main Data2.data.phys_veh.order_id
execute if data storage midwut:main Data2.data.phys_veh.input_id store result score @s phys_veh.id.input run data get storage midwut:main Data2.data.phys_veh.input_id
execute if score @s phys_veh.id.order = @s phys_veh.id.order run tag @s add phys_veh.custom_order

data modify entity @s CustomName set from storage midwut:main vehicle_name