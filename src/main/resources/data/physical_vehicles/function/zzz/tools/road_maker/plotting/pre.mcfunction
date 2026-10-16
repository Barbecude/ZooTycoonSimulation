
scoreboard players operation #t main_score = @s phys_veh.normalize_timer
scoreboard players operation #make main_score = @s phys_veh.wheel.temperature
scoreboard players operation #making main_score = @s phys_veh.wheel.temperature0
scoreboard players operation #seat_id main_score = @s phys_veh.id.seat

execute unless entity @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=!phys_veh.road_maker.spawn,tag=!phys_veh.road_maker.first,predicate=physical_vehicles:same_scores/seat_id] run return fail

execute if score #make main_score matches 0 run scoreboard players set #t main_score 0

data modify storage midwut:main Pos set from entity @s Pos
execute store result score #p0.x main_score run data get storage midwut:main Pos[0] 100
execute store result score #p0.y main_score run data get storage midwut:main Pos[1] 100
execute store result score #p0.z main_score run data get storage midwut:main Pos[2] 100
data modify storage midwut:main Pos set from entity @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=!phys_veh.road_maker.first,limit=1,predicate=physical_vehicles:same_scores/seat_id] Pos
execute store result score #p1.x main_score run data get storage midwut:main Pos[0] 100
execute store result score #p1.y main_score run data get storage midwut:main Pos[1] 100
execute store result score #p1.z main_score run data get storage midwut:main Pos[2] 100

scoreboard players operation #m0.x main_score = @s phys_veh.velocity.x
scoreboard players operation #m0.y main_score = @s phys_veh.velocity.y
scoreboard players operation #m0.z main_score = @s phys_veh.velocity.z

scoreboard players operation #m1.x main_score = @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=!phys_veh.road_maker.first,limit=1,predicate=physical_vehicles:same_scores/seat_id] phys_veh.velocity.x
scoreboard players operation #m1.y main_score = @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=!phys_veh.road_maker.first,limit=1,predicate=physical_vehicles:same_scores/seat_id] phys_veh.velocity.y
scoreboard players operation #m1.z main_score = @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=!phys_veh.road_maker.first,limit=1,predicate=physical_vehicles:same_scores/seat_id] phys_veh.velocity.z

scoreboard players operation #m0.x main_score *= @s phys_veh.hp
scoreboard players operation #m0.y main_score *= @s phys_veh.hp
scoreboard players operation #m0.z main_score *= @s phys_veh.hp
scoreboard players operation #m0.x main_score /= #10000 const
scoreboard players operation #m0.y main_score /= #10000 const
scoreboard players operation #m0.z main_score /= #10000 const

scoreboard players operation #m1.x main_score *= @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=!phys_veh.road_maker.first,limit=1,predicate=physical_vehicles:same_scores/seat_id] phys_veh.hp
scoreboard players operation #m1.y main_score *= @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=!phys_veh.road_maker.first,limit=1,predicate=physical_vehicles:same_scores/seat_id] phys_veh.hp
scoreboard players operation #m1.z main_score *= @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=!phys_veh.road_maker.first,limit=1,predicate=physical_vehicles:same_scores/seat_id] phys_veh.hp
scoreboard players operation #m1.x main_score /= #10000 const
scoreboard players operation #m1.y main_score /= #10000 const
scoreboard players operation #m1.z main_score /= #10000 const

execute positioned as @s run particle minecraft:wax_on
execute if score #make main_score matches 0 if entity @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=phys_veh.road_maker.make,predicate=physical_vehicles:same_scores/seat_id,limit=1,scores={phys_veh.bounding_box.x=0}] run tag @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=phys_veh.road_maker.make,predicate=physical_vehicles:same_scores/seat_id] remove phys_veh.road_maker.make
execute store result score #make main_score if entity @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=phys_veh.road_maker.make,predicate=physical_vehicles:same_scores/seat_id,limit=1]

scoreboard players operation #angle0 main_score = @s phys_veh.add_angle.z
scoreboard players operation #angle1 main_score = @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=!phys_veh.road_maker.first,limit=1,predicate=physical_vehicles:same_scores/seat_id] phys_veh.add_angle.z

kill @e[type=minecraft:marker,tag=phys_veh.road_maker.temp_marker]



execute if score #make main_score matches 1 if score #making main_score matches 0 run function physical_vehicles:zzz/tools/road_maker/plotting/block_pre
function physical_vehicles:zzz/tools/road_maker/plotting/main

execute if score #make main_score matches 1.. if score #making main_score matches 1 if score #t main_score matches 10001.. run scoreboard players set #making main_score 2

execute if score #make main_score matches 1.. if score #making main_score matches 2 run function physical_vehicles:zzz/tools/road_maker/make/pre
execute if score #make main_score matches 1.. if score #making main_score matches 0 run scoreboard players set #making main_score 1

scoreboard players operation @s phys_veh.normalize_timer = #t main_score
scoreboard players operation @s phys_veh.wheel.temperature = #make main_score
scoreboard players operation @s phys_veh.wheel.temperature0 = #making main_score
