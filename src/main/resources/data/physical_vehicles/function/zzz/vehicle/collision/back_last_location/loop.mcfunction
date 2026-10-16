execute store result score #offset.x main_score run data get storage midwut:main collision[-1][0]
execute store result score #offset.y main_score run data get storage midwut:main collision[-1][1]
execute store result score #offset.z main_score run data get storage midwut:main collision[-1][2]

scoreboard players operation #r.x main_score = #m00 main_score
scoreboard players operation #r.x main_score *= #offset.x main_score
scoreboard players operation #tx2 main_score = #m01 main_score
scoreboard players operation #tx2 main_score *= #offset.y main_score
scoreboard players operation #tx3 main_score = #m02 main_score
scoreboard players operation #tx3 main_score *= #offset.z main_score

scoreboard players operation #r.y main_score = #m10 main_score
scoreboard players operation #r.y main_score *= #offset.x main_score
scoreboard players operation #ty2 main_score = #m11 main_score
scoreboard players operation #ty2 main_score *= #offset.y main_score
scoreboard players operation #ty3 main_score = #m12 main_score
scoreboard players operation #ty3 main_score *= #offset.z main_score

scoreboard players operation #r.z main_score = #m20 main_score
scoreboard players operation #r.z main_score *= #offset.x main_score
scoreboard players operation #tz2 main_score = #m21 main_score
scoreboard players operation #tz2 main_score *= #offset.y main_score
scoreboard players operation #tz3 main_score = #m22 main_score
scoreboard players operation #tz3 main_score *= #offset.z main_score

scoreboard players operation #r.x main_score += #tx2 main_score
execute store result storage midwut:main macros.x_s double 0.0000001 run scoreboard players operation #r.x main_score += #tx3 main_score

scoreboard players operation #r.y main_score += #ty2 main_score
execute store result storage midwut:main macros.y_s double 0.0000001 run scoreboard players operation #r.y main_score += #ty3 main_score

scoreboard players operation #r.z main_score += #tz2 main_score
execute store result storage midwut:main macros.z_s double 0.0000001 run scoreboard players operation #r.z main_score += #tz3 main_score

execute unless function physical_vehicles:zzz/vehicle/collision/back_last_location/check_0 run return run tag @s add phys_veh.back_last_location

data remove storage midwut:main collision[-1]
execute if data storage midwut:main collision[0] run function physical_vehicles:zzz/vehicle/collision/back_last_location/loop