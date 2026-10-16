
scoreboard players set #x4 main_score 0
scoreboard players set #y4 main_score 0
scoreboard players set #z4 main_score 0


scoreboard players operation #x2 main_score = #m00_3 main_score
scoreboard players operation #x2 main_score *= #x4 main_score
scoreboard players operation #tx2 main_score = #m01_3 main_score
scoreboard players operation #tx2 main_score *= #y4 main_score
scoreboard players operation #tx3 main_score = #m02_3 main_score
scoreboard players operation #tx3 main_score *= #z4 main_score

scoreboard players operation #y2 main_score = #m10_3 main_score
scoreboard players operation #y2 main_score *= #x4 main_score
scoreboard players operation #ty2 main_score = #m11_3 main_score
scoreboard players operation #ty2 main_score *= #y4 main_score
scoreboard players operation #ty3 main_score = #m12_3 main_score
scoreboard players operation #ty3 main_score *= #z4 main_score

scoreboard players operation #z2 main_score = #m20_3 main_score
scoreboard players operation #z2 main_score *= #x4 main_score
scoreboard players operation #tz2 main_score = #m21_3 main_score
scoreboard players operation #tz2 main_score *= #y4 main_score
scoreboard players operation #tz3 main_score = #m22_3 main_score
scoreboard players operation #tz3 main_score *= #z4 main_score

scoreboard players operation #x2 main_score += #tx2 main_score
scoreboard players operation #x2 main_score += #tx3 main_score

scoreboard players operation #y2 main_score += #ty2 main_score
scoreboard players operation #y2 main_score += #ty3 main_score

scoreboard players operation #z2 main_score += #tz2 main_score
scoreboard players operation #z2 main_score += #tz3 main_score

scoreboard players operation #x2 main_score /= #1000 const
scoreboard players operation #y2 main_score /= #1000 const
scoreboard players operation #z2 main_score /= #1000 const


scoreboard players set #x main_score 7800
scoreboard players set #y main_score 28280
scoreboard players set #z main_score 664050
scoreboard players operation #x main_score -= #x2 main_score
scoreboard players operation #y main_score -= #y2 main_score
scoreboard players operation #z main_score -= #z2 main_score
scoreboard players operation #x main_score -= #main.pos.x main_score
scoreboard players operation #y main_score -= #main.pos.y main_score
scoreboard players operation #z main_score -= #main.pos.z main_score
scoreboard players operation #x main_score -= @s phys_veh.velocity.x
scoreboard players operation #y main_score -= @s phys_veh.velocity.y
scoreboard players operation #z main_score -= @s phys_veh.velocity.z
scoreboard players operation #x main_score *= #3000 const
scoreboard players operation #y main_score *= #3000 const
scoreboard players operation #z main_score *= #3000 const

scoreboard players set #x2 main_score 0
scoreboard players set #y2 main_score 0
scoreboard players set #z2 main_score 0
#function physical_vehicles:zzz/vehicle/math/apply_force/main
