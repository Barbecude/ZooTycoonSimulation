scoreboard players operation #id_test main_score = @s phys_veh.id.vehicle
scoreboard players operation #seat_id main_score = @s phys_veh.id.seat

execute unless entity @s[y_rotation=0.00001,x_rotation=-0.00001,scores={phys_veh.spin.z=1}] run function physical_vehicles:zzz/tools/road_maker/new_rotation

execute unless entity @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=phys_veh.road_maker.first,limit=1,predicate=physical_vehicles:same_scores/seat_id] run tag @s add phys_veh.road_maker.first

function physical_vehicles:zzz/vehicle/math/quaternions/to_matrix
data modify storage midwut:main left_rotation set value [0f,0f,0f,1f]
# data modify storage midwut:main Rotation set value [0f,0f]
execute store result storage midwut:main left_rotation[0] float 0.0001 run scoreboard players get @s phys_veh.quaternion.x
execute store result storage midwut:main left_rotation[1] float 0.0001 run scoreboard players get @s phys_veh.quaternion.y
execute store result storage midwut:main left_rotation[2] float 0.0001 run scoreboard players get @s phys_veh.quaternion.z
execute store result storage midwut:main left_rotation[3] float 0.0001 run scoreboard players get @s phys_veh.quaternion.w
# execute store result storage midwut:main Rotation[0] float 0.001 run scoreboard players get @s phys_veh.add_angle.y
# execute store result storage midwut:main Rotation[1] float 0.001 run scoreboard players get @s phys_veh.add_angle.x

data modify entity @s transformation.left_rotation set from storage midwut:main left_rotation
execute store result entity @s transformation.scale[2] float 0.001 run scoreboard players get @s phys_veh.hp
# data modify entity @s Rotation set from storage midwut:main Rotation
data modify entity @e[type=minecraft:item_display,tag=phys_veh.road_maker.rotation_display,limit=1,predicate=physical_vehicles:same_scores/vehicle_id] transformation.left_rotation set from storage midwut:main left_rotation
# data modify entity @e[type=minecraft:item_display,tag=phys_veh.road_maker.rotation_display,limit=1,predicate=physical_vehicles:same_scores/vehicle_id] Rotation set from storage midwut:main Rotation

data modify storage midwut:main Pos set from entity @s Pos
execute store result score #main.pos.x main_score run data get storage midwut:main Pos[0] 1000
execute store result score #main.pos.y main_score run data get storage midwut:main Pos[1] 1000
execute store result score #main.pos.z main_score run data get storage midwut:main Pos[2] 1000
data modify entity @e[type=minecraft:item_display,tag=phys_veh.road_maker.rotation_display,limit=1,predicate=physical_vehicles:same_scores/vehicle_id] Pos set from storage midwut:main Pos

scoreboard players set #offset_.x main_score 0
scoreboard players set #offset_.y main_score 0
scoreboard players set #offset_.z main_score 500
scoreboard players operation #offset_.z main_score *= @s phys_veh.hp
scoreboard players operation #offset_.z main_score /= #1000 const

scoreboard players operation #offset.x main_score = #m00 main_score
scoreboard players operation #offset.x main_score *= #offset_.x main_score
scoreboard players operation #tx2 main_score = #m01 main_score
scoreboard players operation #tx2 main_score *= #offset_.y main_score
scoreboard players operation #tx3 main_score = #m02 main_score
scoreboard players operation #tx3 main_score *= #offset_.z main_score

scoreboard players operation #offset.y main_score = #m10 main_score
scoreboard players operation #offset.y main_score *= #offset_.x main_score
scoreboard players operation #ty2 main_score = #m11 main_score
scoreboard players operation #ty2 main_score *= #offset_.y main_score
scoreboard players operation #ty3 main_score = #m12 main_score
scoreboard players operation #ty3 main_score *= #offset_.z main_score

scoreboard players operation #offset.z main_score = #m20 main_score
scoreboard players operation #offset.z main_score *= #offset_.x main_score
scoreboard players operation #tz2 main_score = #m21 main_score
scoreboard players operation #tz2 main_score *= #offset_.y main_score
scoreboard players operation #tz3 main_score = #m22 main_score
scoreboard players operation #tz3 main_score *= #offset_.z main_score

scoreboard players operation #offset.x main_score += #tx2 main_score
execute store result storage midwut:main Pos[0] double 0.0000001 run scoreboard players operation #offset.x main_score += #tx3 main_score

scoreboard players operation #offset.y main_score += #ty2 main_score
execute store result storage midwut:main Pos[1] double 0.0000001 run scoreboard players operation #offset.y main_score += #ty3 main_score

scoreboard players operation #offset.z main_score += #tz2 main_score
execute store result storage midwut:main Pos[2] double 0.0000001 run scoreboard players operation #offset.z main_score += #tz3 main_score

data modify entity @s transformation.translation set from storage midwut:main Pos


scoreboard players set #offset_.x main_score 250
scoreboard players set #offset_.y main_score 0
scoreboard players set #offset_.z main_score 0

scoreboard players operation #offset.x main_score = #m00 main_score
scoreboard players operation #offset.x main_score *= #offset_.x main_score
scoreboard players operation #tx2 main_score = #m01 main_score
scoreboard players operation #tx2 main_score *= #offset_.y main_score
scoreboard players operation #tx3 main_score = #m02 main_score
scoreboard players operation #tx3 main_score *= #offset_.z main_score

scoreboard players operation #offset.y main_score = #m10 main_score
scoreboard players operation #offset.y main_score *= #offset_.x main_score
scoreboard players operation #ty2 main_score = #m11 main_score
scoreboard players operation #ty2 main_score *= #offset_.y main_score
scoreboard players operation #ty3 main_score = #m12 main_score
scoreboard players operation #ty3 main_score *= #offset_.z main_score

scoreboard players operation #offset.z main_score = #m20 main_score
scoreboard players operation #offset.z main_score *= #offset_.x main_score
scoreboard players operation #tz2 main_score = #m21 main_score
scoreboard players operation #tz2 main_score *= #offset_.y main_score
scoreboard players operation #tz3 main_score = #m22 main_score
scoreboard players operation #tz3 main_score *= #offset_.z main_score

scoreboard players operation #offset.x main_score += #tx2 main_score
execute store result storage midwut:main Pos[0] double 0.0000001 run scoreboard players operation #offset.x main_score += #tx3 main_score

scoreboard players operation #offset.y main_score += #ty2 main_score
execute store result storage midwut:main Pos[1] double 0.0000001 run scoreboard players operation #offset.y main_score += #ty3 main_score

scoreboard players operation #offset.z main_score += #tz2 main_score
execute store result storage midwut:main Pos[2] double 0.0000001 run scoreboard players operation #offset.z main_score += #tz3 main_score

data modify entity @e[type=minecraft:item_display,tag=phys_veh.road_maker.rotation_display,limit=1,predicate=physical_vehicles:same_scores/vehicle_id] transformation.translation set from storage midwut:main Pos


scoreboard players set #offset_.x main_score 437
scoreboard players set #offset_.y main_score -63
scoreboard players set #offset_.z main_score 0

scoreboard players operation #offset.x main_score = #m00 main_score
scoreboard players operation #offset.x main_score *= #offset_.x main_score
scoreboard players operation #tx2 main_score = #m01 main_score
scoreboard players operation #tx2 main_score *= #offset_.y main_score
scoreboard players operation #tx3 main_score = #m02 main_score
scoreboard players operation #tx3 main_score *= #offset_.z main_score

scoreboard players operation #offset.y main_score = #m10 main_score
scoreboard players operation #offset.y main_score *= #offset_.x main_score
scoreboard players operation #ty2 main_score = #m11 main_score
scoreboard players operation #ty2 main_score *= #offset_.y main_score
scoreboard players operation #ty3 main_score = #m12 main_score
scoreboard players operation #ty3 main_score *= #offset_.z main_score

scoreboard players operation #offset.z main_score = #m20 main_score
scoreboard players operation #offset.z main_score *= #offset_.x main_score
scoreboard players operation #tz2 main_score = #m21 main_score
scoreboard players operation #tz2 main_score *= #offset_.y main_score
scoreboard players operation #tz3 main_score = #m22 main_score
scoreboard players operation #tz3 main_score *= #offset_.z main_score

scoreboard players operation #offset.x main_score += #tx2 main_score
scoreboard players operation #offset.x main_score += #tx3 main_score

scoreboard players operation #offset.y main_score += #ty2 main_score
scoreboard players operation #offset.y main_score += #ty3 main_score

scoreboard players operation #offset.z main_score += #tz2 main_score
scoreboard players operation #offset.z main_score += #tz3 main_score

scoreboard players operation #offset.x main_score /= #10000 const
scoreboard players operation #offset.y main_score /= #10000 const
scoreboard players operation #offset.z main_score /= #10000 const

execute store result storage midwut:main Pos[0] double 0.001 run scoreboard players operation #offset.x main_score += #main.pos.x main_score
execute store result storage midwut:main Pos[1] double 0.001 run scoreboard players operation #offset.y main_score += #main.pos.y main_score
execute store result storage midwut:main Pos[2] double 0.001 run scoreboard players operation #offset.z main_score += #main.pos.z main_score
data modify entity @e[type=minecraft:interaction,tag=phys_veh.road_maker.rotation_interaction,limit=1,predicate=physical_vehicles:same_scores/vehicle_id] Pos set from storage midwut:main Pos


scoreboard players set #offset_.x main_score 0
scoreboard players set #offset_.y main_score -63
scoreboard players set #offset_.z main_score 1000
scoreboard players operation #offset_.z main_score *= @s phys_veh.hp
scoreboard players operation #offset_.z main_score /= #1000 const
scoreboard players remove #offset_.z main_score 63

scoreboard players operation #offset.x main_score = #m00 main_score
scoreboard players operation #offset.x main_score *= #offset_.x main_score
scoreboard players operation #tx2 main_score = #m01 main_score
scoreboard players operation #tx2 main_score *= #offset_.y main_score
scoreboard players operation #tx3 main_score = #m02 main_score
scoreboard players operation #tx3 main_score *= #offset_.z main_score

scoreboard players operation #offset.y main_score = #m10 main_score
scoreboard players operation #offset.y main_score *= #offset_.x main_score
scoreboard players operation #ty2 main_score = #m11 main_score
scoreboard players operation #ty2 main_score *= #offset_.y main_score
scoreboard players operation #ty3 main_score = #m12 main_score
scoreboard players operation #ty3 main_score *= #offset_.z main_score

scoreboard players operation #offset.z main_score = #m20 main_score
scoreboard players operation #offset.z main_score *= #offset_.x main_score
scoreboard players operation #tz2 main_score = #m21 main_score
scoreboard players operation #tz2 main_score *= #offset_.y main_score
scoreboard players operation #tz3 main_score = #m22 main_score
scoreboard players operation #tz3 main_score *= #offset_.z main_score

scoreboard players operation #offset.x main_score += #tx2 main_score
scoreboard players operation #offset.x main_score += #tx3 main_score

scoreboard players operation #offset.y main_score += #ty2 main_score
scoreboard players operation #offset.y main_score += #ty3 main_score

scoreboard players operation #offset.z main_score += #tz2 main_score
scoreboard players operation #offset.z main_score += #tz3 main_score

scoreboard players operation #offset.x main_score /= #10000 const
scoreboard players operation #offset.y main_score /= #10000 const
scoreboard players operation #offset.z main_score /= #10000 const

execute store result storage midwut:main Pos[0] double 0.001 run scoreboard players operation #offset.x main_score += #main.pos.x main_score
execute store result storage midwut:main Pos[1] double 0.001 run scoreboard players operation #offset.y main_score += #main.pos.y main_score
execute store result storage midwut:main Pos[2] double 0.001 run scoreboard players operation #offset.z main_score += #main.pos.z main_score

data modify entity @e[type=minecraft:interaction,tag=phys_veh.road_maker.direction_interaction,limit=1,predicate=physical_vehicles:same_scores/vehicle_id] Pos set from storage midwut:main Pos

execute as @e[type=minecraft:interaction,tag=phys_veh.road_maker.direction_interaction,limit=1,predicate=physical_vehicles:same_scores/vehicle_id] run function physical_vehicles:zzz/tools/road_maker/direction_interaction/tick
execute as @e[type=minecraft:interaction,tag=phys_veh.road_maker.rotation_interaction,limit=1,predicate=physical_vehicles:same_scores/vehicle_id] run function physical_vehicles:zzz/tools/road_maker/direction_interaction/tick