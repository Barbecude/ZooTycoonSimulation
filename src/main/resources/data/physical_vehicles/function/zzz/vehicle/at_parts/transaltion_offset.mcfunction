#> physical_vehicles:zzz/vehicle/at_parts/offset

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
execute store result storage midwut:main Data.data.transformation.translation[0] float 0.0000001 run scoreboard players operation #offset.x main_score += #tx3 main_score

scoreboard players operation #offset.y main_score += #ty2 main_score
execute store result storage midwut:main Data.data.transformation.translation[1] float 0.0000001 run scoreboard players operation #offset.y main_score += #ty3 main_score

scoreboard players operation #offset.z main_score += #tz2 main_score
execute store result storage midwut:main Data.data.transformation.translation[2] float 0.0000001 run scoreboard players operation #offset.z main_score += #tz3 main_score

data modify entity @s transformation.translation set from storage midwut:main Data.data.transformation.translation
