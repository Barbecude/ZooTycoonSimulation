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
scoreboard players operation #r.x main_score += #tx3 main_score

scoreboard players operation #r.y main_score += #ty2 main_score
scoreboard players operation #r.y main_score += #ty3 main_score

scoreboard players operation #r.z main_score += #tz2 main_score
scoreboard players operation #r.z main_score += #tz3 main_score

scoreboard players operation #r.x main_score /= #10000 const
scoreboard players operation #r.y main_score /= #10000 const
scoreboard players operation #r.z main_score /= #10000 const



scoreboard players operation #vp.z main_score = #main.rotated_spin.x main_score
scoreboard players operation #vp.x main_score = #main.rotated_spin.y main_score
scoreboard players operation #vp.y main_score = #main.rotated_spin.z main_score

scoreboard players operation #vp.x main_score *= #r.z main_score
scoreboard players operation #temp main_score = #vp.y main_score
scoreboard players operation #temp main_score *= #r.y main_score
scoreboard players operation #vp.x main_score -= #temp main_score
scoreboard players operation #vp.x main_score /= #10000 const

scoreboard players operation #vp.y main_score *= #r.x main_score
scoreboard players operation #temp main_score = #vp.z main_score
scoreboard players operation #temp main_score *= #r.z main_score
scoreboard players operation #vp.y main_score -= #temp main_score
scoreboard players operation #vp.y main_score /= #10000 const

scoreboard players operation #vp.z main_score *= #r.y main_score
scoreboard players operation #temp main_score = #main.rotated_spin.y main_score
scoreboard players operation #temp main_score *= #r.x main_score
scoreboard players operation #vp.z main_score -= #temp main_score
scoreboard players operation #vp.z main_score /= #10000 const



scoreboard players operation #pos.x main_score = #r.x main_score
scoreboard players operation #vp.x main_score += #main.velocity.x main_score
scoreboard players operation #vp_dt.x main_score = #vp.x main_score
scoreboard players operation #vp_dt.x main_score /= #tick_rate main_score
scoreboard players operation #pos.x main_score += #vp_dt.x main_score
execute store result storage midwut:main macros.x_f double 0.001 run scoreboard players operation #pos.x main_score += #main.pos.x main_score
scoreboard players operation #pos.y main_score = #r.y main_score
scoreboard players operation #vp.y main_score += #main.velocity.y main_score
scoreboard players operation #vp_dt.y main_score = #vp.y main_score
scoreboard players operation #vp_dt.y main_score /= #tick_rate main_score
scoreboard players operation #pos.y main_score += #vp_dt.y main_score
execute store result storage midwut:main macros.y_f double 0.001 run scoreboard players operation #pos.y main_score += #main.pos.y main_score
scoreboard players operation #pos.z main_score = #r.z main_score
scoreboard players operation #vp.z main_score += #main.velocity.z main_score
scoreboard players operation #vp_dt.z main_score = #vp.z main_score
scoreboard players operation #vp_dt.z main_score /= #tick_rate main_score
scoreboard players operation #pos.z main_score += #vp_dt.z main_score
execute store result storage midwut:main macros.z_f double 0.001 run scoreboard players operation #pos.z main_score += #main.pos.z main_score

execute if entity @s[tag=phys_veh.debug.particles] run function physical_vehicles:zzz/vehicle/collision/vertices/particles with storage midwut:main macros
function physical_vehicles:zzz/vehicle/collision/vertices/collision_choice



data remove storage midwut:main collision[-1]
execute if data storage midwut:main collision[0] run function physical_vehicles:zzz/vehicle/collision/vertices/loop_main