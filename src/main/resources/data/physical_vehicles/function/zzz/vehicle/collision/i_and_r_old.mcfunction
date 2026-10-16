


# scoreboard players set #j main_score 1000
# scoreboard players operation #j main_score += @s phys_veh.coeff_e
# scoreboard players operation #j main_score *= #col_ny_v main_score
# scoreboard players operation #j main_score /= #1000 const
# scoreboard players operation @s phys_veh.velocity.y -= #j main_score


# scoreboard players operation #x3 main_score = #col_ny_z main_score
# scoreboard players operation #x3 main_score *= #j main_score
# scoreboard players operation #x3 main_score /= #1000 const

# scoreboard players operation #z3 main_score = #col_ny_x main_score
# scoreboard players operation #z3 main_score *= #j main_score
# scoreboard players operation #z3 main_score /= #-1000 const


# scoreboard players operation #offset.x main_score = #x3 main_score
# scoreboard players set #offset.y main_score 0
# scoreboard players operation #offset.z main_score = #z3 main_score










scoreboard players operation #x3 main_score = #m00_3 main_score
scoreboard players operation #x3 main_score *= #offset.x main_score
scoreboard players operation #tx2 main_score = #m10_3 main_score
scoreboard players operation #tx2 main_score *= #offset.y main_score
scoreboard players operation #tx3 main_score = #m20_3 main_score
scoreboard players operation #tx3 main_score *= #offset.z main_score

scoreboard players operation #y3 main_score = #m01_3 main_score
scoreboard players operation #y3 main_score *= #offset.x main_score
scoreboard players operation #ty2 main_score = #m11_3 main_score
scoreboard players operation #ty2 main_score *= #offset.y main_score
scoreboard players operation #ty3 main_score = #m21_3 main_score
scoreboard players operation #ty3 main_score *= #offset.z main_score

scoreboard players operation #z3 main_score = #m02_3 main_score
scoreboard players operation #z3 main_score *= #offset.x main_score
scoreboard players operation #tz2 main_score = #m12_3 main_score
scoreboard players operation #tz2 main_score *= #offset.y main_score
scoreboard players operation #tz3 main_score = #m22_3 main_score
scoreboard players operation #tz3 main_score *= #offset.z main_score

scoreboard players operation #x3 main_score += #tx2 main_score
scoreboard players operation #x3 main_score += #tx3 main_score

scoreboard players operation #y3 main_score += #ty2 main_score
scoreboard players operation #y3 main_score += #ty3 main_score

scoreboard players operation #z3 main_score += #tz2 main_score
scoreboard players operation #z3 main_score += #tz3 main_score

scoreboard players operation #x3 main_score *= #10 const
scoreboard players operation #y3 main_score *= #10 const
scoreboard players operation #z3 main_score *= #10 const
scoreboard players operation #x3 main_score /= @s phys_veh.moment_of_inertia_op.x
scoreboard players operation #y3 main_score /= @s phys_veh.moment_of_inertia_op.y
scoreboard players operation #z3 main_score /= @s phys_veh.moment_of_inertia_op.z
# scoreboard players operation #x3 main_score *= @s phys_veh.mass
# scoreboard players operation #y3 main_score *= @s phys_veh.mass
# scoreboard players operation #z3 main_score *= @s phys_veh.mass
# scoreboard players operation #x3 main_score /= #1200 const
# scoreboard players operation #y3 main_score /= #1200 const
# scoreboard players operation #z3 main_score /= #1200 const

scoreboard players operation @s phys_veh.spin.x += #x3 main_score
scoreboard players operation @s phys_veh.spin.y += #y3 main_score
scoreboard players operation @s phys_veh.spin.z += #z3 main_score

