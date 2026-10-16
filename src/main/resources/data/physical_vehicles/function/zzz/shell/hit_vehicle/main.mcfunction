scoreboard players operation #vx main_score = #x main_score
scoreboard players operation #vy main_score = #y main_score
scoreboard players operation #vz main_score = #z main_score

data modify storage midwut:main Pos set from entity @s Pos
execute store result score #temp main_score run data get storage midwut:main Pos[0] 1000
scoreboard players operation #vx main_score -= #temp main_score
execute store result score #temp main_score run data get storage midwut:main Pos[1] 1000
scoreboard players operation #vy main_score -= #temp main_score
execute store result score #temp main_score run data get storage midwut:main Pos[2] 1000
scoreboard players operation #vz main_score -= #temp main_score

scoreboard players operation #x2 main_score = @s phys_veh.m00
scoreboard players operation #x2 main_score *= #vx main_score
scoreboard players operation #tx2 main_score = @s phys_veh.m10
scoreboard players operation #tx2 main_score *= #vy main_score
scoreboard players operation #tx3 main_score = @s phys_veh.m20
scoreboard players operation #tx3 main_score *= #vz main_score

scoreboard players operation #y2 main_score = @s phys_veh.m01
scoreboard players operation #y2 main_score *= #vx main_score
scoreboard players operation #ty2 main_score = @s phys_veh.m11
scoreboard players operation #ty2 main_score *= #vy main_score
scoreboard players operation #ty3 main_score = @s phys_veh.m21
scoreboard players operation #ty3 main_score *= #vz main_score

scoreboard players operation #z2 main_score = @s phys_veh.m02
scoreboard players operation #z2 main_score *= #vx main_score
scoreboard players operation #tz2 main_score = @s phys_veh.m12
scoreboard players operation #tz2 main_score *= #vy main_score
scoreboard players operation #tz3 main_score = @s phys_veh.m22
scoreboard players operation #tz3 main_score *= #vz main_score

scoreboard players operation #x2 main_score += #tx2 main_score
scoreboard players operation #x2 main_score += #tx3 main_score

scoreboard players operation #y2 main_score += #ty2 main_score
scoreboard players operation #y2 main_score += #ty3 main_score

scoreboard players operation #z2 main_score += #tz2 main_score
scoreboard players operation #z2 main_score += #tz3 main_score

scoreboard players operation #x2 main_score /= #10000 const
scoreboard players operation #y2 main_score /= #10000 const
scoreboard players operation #z2 main_score /= #10000 const


data modify storage midwut:main boxes set from entity @s data.phys_veh.armor_collisions
function physical_vehicles:zzz/shell/hit_vehicle/loop