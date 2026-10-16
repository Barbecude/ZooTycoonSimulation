execute if entity @a[tag=midwut.used.dolphin_spawn_egg.selected_player,predicate=midwut:check/input/sneak,limit=1] run return run function physical_vehicles:zzz/tools/road_maker/kill
scoreboard players operation @s phys_veh.id.seat = @a[tag=midwut.used.dolphin_spawn_egg.selected_player,limit=1] midwut.player_id
scoreboard players operation #seat_id main_score = @s phys_veh.id.seat
execute store result score #road_maker_count main_score if entity @e[tag=phys_veh.road_maker.main,predicate=physical_vehicles:same_scores/seat_id]
execute if score #road_maker_count main_score matches 3.. run return run kill @s

data merge entity @s {item:{id:"minecraft:blue_stained_glass"},transformation:{translation:[0f,0f,0.5f],left_rotation:[0f,0f,0f,1f],scale:[0.0625f,0.0625f,1.0f],right_rotation:[0f,0f,0f,1f]}}
summon minecraft:item_display ~ ~ ~ {item:{id:"minecraft:red_stained_glass"},Tags:["phys_veh.road_maker","phys_veh.road_maker.rotation_display","get_id"],transformation:{translation:[0.25f,0.0f,0f],left_rotation:[0f,0f,0f,1f],scale:[0.5f,0.0625f,0.0625f],right_rotation:[0f,0f,0f,1f]}}
summon minecraft:interaction ~ ~ ~ {Tags:["phys_veh.road_maker","phys_veh.road_maker.rotation_interaction","get_id"],width:0.125f,height:0.125f,response:true}
summon minecraft:interaction ~ ~ ~ {Tags:["phys_veh.road_maker","phys_veh.road_maker.direction_interaction","get_id"],width:0.125f,height:0.125f,response:true}

scoreboard players add #physical_vehicles.id.road_maker main_score 1
scoreboard players operation @s phys_veh.id.vehicle = #physical_vehicles.id.road_maker main_score
scoreboard players operation @e[tag=get_id,distance=..0.1] phys_veh.id.vehicle = #physical_vehicles.id.road_maker main_score
scoreboard players operation @e[tag=get_id,distance=..0.1] phys_veh.id.seat = #seat_id main_score
tag @e[tag=get_id,distance=..0.1] remove get_id

tag @s remove phys_veh.road_maker.spawn
scoreboard players set @s phys_veh.quaternion.x 0
scoreboard players set @s phys_veh.quaternion.y 0
scoreboard players set @s phys_veh.quaternion.z 0
scoreboard players set @s phys_veh.quaternion.w 10000

scoreboard players set @s phys_veh.velocity.x 0
scoreboard players set @s phys_veh.velocity.y 0
scoreboard players set @s phys_veh.velocity.z 1000

scoreboard players set @s phys_veh.add_angle.x 0
scoreboard players set @s phys_veh.add_angle.y 0
scoreboard players set @s phys_veh.add_angle.z 0

scoreboard players set @s phys_veh.hp 1000
scoreboard players set @s phys_veh.bounding_box.x 0

scoreboard players set @s phys_veh.wheel.temperature 0
scoreboard players set @s phys_veh.wheel.temperature0 0
scoreboard players set @s phys_veh.normalize_timer 0

rotate @s 0.00001 -0.00001
scoreboard players set @s phys_veh.spin.z 1

schedule function physical_vehicles:zzz/tools/road_maker/tick 1t replace