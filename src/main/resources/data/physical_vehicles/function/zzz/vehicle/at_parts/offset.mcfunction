#> physical_vehicles:zzz/vehicle/at_parts/offset
execute if entity @s[tag=phys_veh.seat] if predicate physical_vehicles:seat_pas_pas run scoreboard players add @s phys_veh.offset.y 875

scoreboard players operation #offset.x main_score = #m00 main_score
scoreboard players operation #offset.x main_score *= @s phys_veh.offset.x
scoreboard players operation #tx2 main_score = #m01 main_score
scoreboard players operation #tx2 main_score *= @s phys_veh.offset.y
scoreboard players operation #tx3 main_score = #m02 main_score
scoreboard players operation #tx3 main_score *= @s phys_veh.offset.z

scoreboard players operation #offset.y main_score = #m10 main_score
scoreboard players operation #offset.y main_score *= @s phys_veh.offset.x
scoreboard players operation #ty2 main_score = #m11 main_score
scoreboard players operation #ty2 main_score *= @s phys_veh.offset.y
scoreboard players operation #ty3 main_score = #m12 main_score
scoreboard players operation #ty3 main_score *= @s phys_veh.offset.z

scoreboard players operation #offset.z main_score = #m20 main_score
scoreboard players operation #offset.z main_score *= @s phys_veh.offset.x
scoreboard players operation #tz2 main_score = #m21 main_score
scoreboard players operation #tz2 main_score *= @s phys_veh.offset.y
scoreboard players operation #tz3 main_score = #m22 main_score
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


#===========================================

#execute if entity @s[tag=!phys_veh.sit,tag=!phys_veh.dont_down] run function physical_vehicles:zzz/vehicle/at_parts/small_remove_y

#execute if entity @s[tag=phys_veh.coll] run function physical_vehicles:zzz/vehicle/at_parts/collision_prediction

#=========================================================



execute if entity @s[tag=!phys_veh.seat] run function physical_vehicles:zzz/vehicle/at_parts/offset/main
execute if entity @s[tag=phys_veh.seat] run function physical_vehicles:zzz/vehicle/at_parts/offset/is_seat

#execute if entity @s[tag=phys_veh.turret] run tellraw @a [{score:{"name":"#offset.x",objective:"main_score"}},"  ",{score:{"name":"#offset.y",objective:"main_score"}},"  ",{score:{"name":"#offset.z",objective:"main_score"}}]

#execute positioned as @s[tag=phys_veh.get_rotation,tag=!phys_veh.get_repeat] run function physical_vehicles:zzz/vehicle/at_parts/offset_get-rot

