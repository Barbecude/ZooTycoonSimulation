#> physical_vehicles:zzz/vehicle/at_parts/offset
execute as @e[type=#physical_vehicles:refers_to_vehicle/all,tag=phys_veh.selected_part,predicate=physical_vehicles:same_scores/object_parent_id,limit=1,x=0] run function physical_vehicles:zzz/vehicle/at_parts/order/matrix_copy

execute store result score #pos.x main_score run data get storage midwut:main Pos[0] 1000
execute store result score #pos.y main_score run data get storage midwut:main Pos[1] 1000
execute store result score #pos.z main_score run data get storage midwut:main Pos[2] 1000

execute if score @s phys_veh.offset2.z = @s phys_veh.offset2.z unless score @s phys_veh.offset2.z matches 0 run scoreboard players operation @s phys_veh.offset.z += @s phys_veh.offset2.z

execute if entity @s[tag=phys_veh.seat] if predicate physical_vehicles:seat_pas_pas run scoreboard players add @s phys_veh.offset.y 875


scoreboard players operation #offset.x main_score = #m00_ main_score
scoreboard players operation #offset.x main_score *= @s phys_veh.offset.x
scoreboard players operation #tx2 main_score = #m01_ main_score
scoreboard players operation #tx2 main_score *= @s phys_veh.offset.y
scoreboard players operation #tx3 main_score = #m02_ main_score
scoreboard players operation #tx3 main_score *= @s phys_veh.offset.z

scoreboard players operation #offset.y main_score = #m10_ main_score
scoreboard players operation #offset.y main_score *= @s phys_veh.offset.x
scoreboard players operation #ty2 main_score = #m11_ main_score
scoreboard players operation #ty2 main_score *= @s phys_veh.offset.y
scoreboard players operation #ty3 main_score = #m12_ main_score
scoreboard players operation #ty3 main_score *= @s phys_veh.offset.z

scoreboard players operation #offset.z main_score = #m20_ main_score
scoreboard players operation #offset.z main_score *= @s phys_veh.offset.x
scoreboard players operation #tz2 main_score = #m21_ main_score
scoreboard players operation #tz2 main_score *= @s phys_veh.offset.y
scoreboard players operation #tz3 main_score = #m22_ main_score
scoreboard players operation #tz3 main_score *= @s phys_veh.offset.z

scoreboard players operation #offset.x main_score += #tx2 main_score
scoreboard players operation #offset.x main_score += #tx3 main_score

scoreboard players operation #offset.y main_score += #ty2 main_score
scoreboard players operation #offset.y main_score += #ty3 main_score

scoreboard players operation #offset.z main_score += #tz2 main_score
scoreboard players operation #offset.z main_score += #tz3 main_score

scoreboard players operation #offset.x main_score /= #10000 const
scoreboard players operation #offset.y main_score /= #10000 const
scoreboard players operation #offset.z main_score /= #10000 const

execute if score @s phys_veh.offset2.z = @s phys_veh.offset2.z unless score @s phys_veh.offset2.z matches 0 run scoreboard players operation @s phys_veh.offset.z -= @s phys_veh.offset2.z

#===========================================

#execute if entity @s[tag=!phys_veh.sit,tag=!phys_veh.dont_down] run function physical_vehicles:zzz/vehicle/at_parts/small_remove_y

#execute if entity @s[tag=phys_veh.coll] run function physical_vehicles:zzz/vehicle/at_parts/collision_prediction

#=========================================================
#execute if entity @s[tag=phys_veh.gun_mantlet] run tellraw @a [{score:{"name":"#pos.x",objective:"main_score"}},"  ",{score:{"name":"#pos.y",objective:"main_score"}},"  ",{score:{"name":"#pos.z",objective:"main_score"}}]

execute if entity @s[tag=!phys_veh.seat] run function physical_vehicles:zzz/vehicle/at_parts/order/offset_default
execute if entity @s[tag=phys_veh.seat] run function physical_vehicles:zzz/vehicle/at_parts/order/offset_seat


#execute positioned as @s[tag=phys_veh.get_rotation,tag=!phys_veh.get_repeat] run function physical_vehicles:zzz/vehicle/at_parts/offset_get-rot

