## f - #x main_score
## f - #y main_score
## f - #z main_score

## r - #x2 main_score
## r - #y2 main_score
## r - #z2 main_score

scoreboard players operation #f.x main_score += #x main_score
scoreboard players operation #f.y main_score += #y main_score
scoreboard players operation #f.z main_score += #z main_score

#scoreboard players operation #x4 main_score = #x main_score
#scoreboard players operation #y4 main_score = #y main_score
#scoreboard players operation #z4 main_score = #z main_score

#scoreboard players operation #x main_score = #m00_3 main_score
#scoreboard players operation #x main_score *= #x4 main_score
#scoreboard players operation #tx2 main_score = #m10_3 main_score
#scoreboard players operation #tx2 main_score *= #y4 main_score
#scoreboard players operation #tx3 main_score = #m20_3 main_score
#scoreboard players operation #tx3 main_score *= #z4 main_score

#scoreboard players operation #y main_score = #m01_3 main_score
#scoreboard players operation #y main_score *= #x4 main_score
#scoreboard players operation #ty2 main_score = #m11_3 main_score
#scoreboard players operation #ty2 main_score *= #y4 main_score
#scoreboard players operation #ty3 main_score = #m21_3 main_score
#scoreboard players operation #ty3 main_score *= #z4 main_score

#scoreboard players operation #z main_score = #m02_3 main_score
#scoreboard players operation #z main_score *= #x4 main_score
#scoreboard players operation #tz2 main_score = #m12_3 main_score
#scoreboard players operation #tz2 main_score *= #y4 main_score
#scoreboard players operation #tz3 main_score = #m22_3 main_score
#scoreboard players operation #tz3 main_score *= #z4 main_score

#scoreboard players operation #x main_score += #tx2 main_score
#scoreboard players operation #x main_score += #tx3 main_score

#scoreboard players operation #y main_score += #ty2 main_score
#scoreboard players operation #y main_score += #ty3 main_score

#scoreboard players operation #z main_score += #tz2 main_score
#scoreboard players operation #z main_score += #tz3 main_score

#scoreboard players operation #x main_score /= #1000 const
#scoreboard players operation #y main_score /= #1000 const
#scoreboard players operation #z main_score /= #1000 const



function physical_vehicles:zzz/vehicle/math/cross_product/main

scoreboard players operation #x3 main_score *= #10 const
scoreboard players operation #y3 main_score *= #10 const
scoreboard players operation #z3 main_score *= #10 const
scoreboard players operation #x3 main_score /= @s phys_veh.moment_of_inertia.x
scoreboard players operation #y3 main_score /= @s phys_veh.moment_of_inertia.y
scoreboard players operation #z3 main_score /= @s phys_veh.moment_of_inertia.z
#scoreboard players operation @s phys_veh.spin.x += #x3 main_score
#scoreboard players operation @s phys_veh.spin.y -= #y3 main_score
#scoreboard players operation @s phys_veh.spin.z -= #z3 main_score

#scoreboard players operation #x main_score *= #100 const
#scoreboard players operation #y main_score *= #100 const
#scoreboard players operation #z main_score *= #100 const
