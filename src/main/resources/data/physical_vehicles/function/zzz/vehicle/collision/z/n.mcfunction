scoreboard players operation #col_nz_x main_score /= #col_nz_c main_score
scoreboard players operation #col_nz_y main_score /= #col_nz_c main_score
scoreboard players operation #col_nz_z main_score /= #col_nz_c main_score
scoreboard players operation #col_nz_v main_score /= #col_nz_c main_score
scoreboard players remove #col_nz_v main_score 50


scoreboard players set #n.x main_score 0
scoreboard players set #n.y main_score 0
scoreboard players set #n.z main_score 1000

scoreboard players set #j main_score 1000
scoreboard players operation #j main_score += @s phys_veh.coeff_e
scoreboard players operation #j main_score *= #col_nz_v main_score
scoreboard players operation #j main_score *= #10 const


scoreboard players set #j_ main_score 1000000
scoreboard players operation #j_ main_score /= @s phys_veh.mass


scoreboard players operation #t.z main_score = #col_nz_x main_score
scoreboard players operation #t.x main_score = #col_nz_y main_score
scoreboard players operation #t.y main_score = #col_nz_z main_score

scoreboard players operation #t.x main_score *= #n.z main_score
scoreboard players operation #temp main_score = #t.y main_score
scoreboard players operation #temp main_score *= #n.y main_score
scoreboard players operation #t.x main_score -= #temp main_score
scoreboard players operation #t.x main_score /= #1000 const

scoreboard players operation #t.y main_score *= #n.x main_score
scoreboard players operation #temp main_score = #t.z main_score
scoreboard players operation #temp main_score *= #n.z main_score
scoreboard players operation #t.y main_score -= #temp main_score
scoreboard players operation #t.y main_score /= #1000 const

scoreboard players operation #t.z main_score *= #n.y main_score
scoreboard players operation #temp main_score = #col_nz_y main_score
scoreboard players operation #temp main_score *= #n.x main_score
scoreboard players operation #t.z main_score -= #temp main_score
scoreboard players operation #t.z main_score /= #1000 const



scoreboard players operation #x3 main_score = #m00_3 main_score
scoreboard players operation #x3 main_score *= #t.x main_score
scoreboard players operation #tx2 main_score = #m10_3 main_score
scoreboard players operation #tx2 main_score *= #t.y main_score
scoreboard players operation #tx3 main_score = #m20_3 main_score
scoreboard players operation #tx3 main_score *= #t.z main_score

scoreboard players operation #y3 main_score = #m01_3 main_score
scoreboard players operation #y3 main_score *= #t.x main_score
scoreboard players operation #ty2 main_score = #m11_3 main_score
scoreboard players operation #ty2 main_score *= #t.y main_score
scoreboard players operation #ty3 main_score = #m21_3 main_score
scoreboard players operation #ty3 main_score *= #t.z main_score

scoreboard players operation #z3 main_score = #m02_3 main_score
scoreboard players operation #z3 main_score *= #t.x main_score
scoreboard players operation #tz2 main_score = #m12_3 main_score
scoreboard players operation #tz2 main_score *= #t.y main_score
scoreboard players operation #tz3 main_score = #m22_3 main_score
scoreboard players operation #tz3 main_score *= #t.z main_score

scoreboard players operation #x3 main_score += #tx2 main_score
scoreboard players operation #x3 main_score += #tx3 main_score

scoreboard players operation #y3 main_score += #ty2 main_score
scoreboard players operation #y3 main_score += #ty3 main_score

scoreboard players operation #z3 main_score += #tz2 main_score
scoreboard players operation #z3 main_score += #tz3 main_score

scoreboard players operation #x3 main_score /= @s phys_veh.moment_of_inertia.x
scoreboard players operation #y3 main_score /= @s phys_veh.moment_of_inertia.y
scoreboard players operation #z3 main_score /= @s phys_veh.moment_of_inertia.z


scoreboard players operation #t.x main_score = #m00_3 main_score
scoreboard players operation #t.x main_score *= #x3 main_score
scoreboard players operation #tx2 main_score = #m01_3 main_score
scoreboard players operation #tx2 main_score *= #y3 main_score
scoreboard players operation #tx3 main_score = #m02_3 main_score
scoreboard players operation #tx3 main_score *= #z3 main_score

scoreboard players operation #t.y main_score = #m10_3 main_score
scoreboard players operation #t.y main_score *= #x3 main_score
scoreboard players operation #ty2 main_score = #m11_3 main_score
scoreboard players operation #ty2 main_score *= #y3 main_score
scoreboard players operation #ty3 main_score = #m12_3 main_score
scoreboard players operation #ty3 main_score *= #z3 main_score

scoreboard players operation #t.z main_score = #m20_3 main_score
scoreboard players operation #t.z main_score *= #x3 main_score
scoreboard players operation #tz2 main_score = #m21_3 main_score
scoreboard players operation #tz2 main_score *= #y3 main_score
scoreboard players operation #tz3 main_score = #m22_3 main_score
scoreboard players operation #tz3 main_score *= #z3 main_score

scoreboard players operation #t.x main_score += #tx2 main_score
scoreboard players operation #t.x main_score += #tx3 main_score

scoreboard players operation #t.y main_score += #ty2 main_score
scoreboard players operation #t.y main_score += #ty3 main_score

scoreboard players operation #t.z main_score += #tz2 main_score
scoreboard players operation #t.z main_score += #tz3 main_score

scoreboard players operation #t.x main_score /= #100 const
scoreboard players operation #t.y main_score /= #100 const
scoreboard players operation #t.z main_score /= #100 const


scoreboard players operation #z3 main_score = #t.x main_score
scoreboard players operation #x3 main_score = #t.y main_score
scoreboard players operation #y3 main_score = #t.z main_score

scoreboard players operation #x3 main_score *= #col_nz_z main_score
scoreboard players operation #temp main_score = #y3 main_score
scoreboard players operation #temp main_score *= #col_nz_y main_score
scoreboard players operation #x3 main_score -= #temp main_score
scoreboard players operation #x3 main_score /= #1000 const

scoreboard players operation #y3 main_score *= #col_nz_x main_score
scoreboard players operation #temp main_score = #z3 main_score
scoreboard players operation #temp main_score *= #col_nz_z main_score
scoreboard players operation #y3 main_score -= #temp main_score
scoreboard players operation #y3 main_score /= #1000 const

scoreboard players operation #z3 main_score *= #col_nz_y main_score
scoreboard players operation #temp main_score = #t.y main_score
scoreboard players operation #temp main_score *= #col_nz_x main_score
scoreboard players operation #z3 main_score -= #temp main_score
scoreboard players operation #z3 main_score /= #1000 const



scoreboard players operation #x3 main_score *= #n.x main_score
scoreboard players operation #y3 main_score *= #n.y main_score
scoreboard players operation #z3 main_score *= #n.z main_score
scoreboard players operation #x3 main_score += #y3 main_score
scoreboard players operation #x3 main_score += #z3 main_score
scoreboard players operation #x3 main_score /= #1000 const
execute if score #x3 main_score matches ..-1 run scoreboard players set #x3 main_score 0
scoreboard players operation #j_ main_score += #x3 main_score



scoreboard players operation #j main_score /= @s phys_veh.mass
scoreboard players operation #j main_score /= #j_ main_score

scoreboard players operation #n.x main_score *= #j main_score
scoreboard players operation #n.y main_score *= #j main_score
scoreboard players operation #n.z main_score *= #j main_score
scoreboard players operation #n.x main_score /= #-10 const
scoreboard players operation #n.y main_score /= #-10 const
scoreboard players operation #n.z main_score /= #-10 const



scoreboard players operation @s phys_veh.velocity.x += #n.x main_score
scoreboard players operation @s phys_veh.velocity.y += #n.y main_score
scoreboard players operation @s phys_veh.velocity.z += #n.z main_score

scoreboard players operation #n2.x main_score = #n.x main_score
scoreboard players operation #n2.y main_score = #n.y main_score
scoreboard players operation #n2.z main_score = #n.z main_score
scoreboard players operation #n2.x main_score /= #10 const
scoreboard players operation #n2.y main_score /= #10 const
scoreboard players operation #n2.z main_score /= #10 const



scoreboard players operation #t.z main_score = #col_nz_x main_score
scoreboard players operation #t.x main_score = #col_nz_y main_score
scoreboard players operation #t.y main_score = #col_nz_z main_score

scoreboard players operation #t.x main_score *= #n2.z main_score
scoreboard players operation #temp main_score = #t.y main_score
scoreboard players operation #temp main_score *= #n2.y main_score
scoreboard players operation #t.x main_score -= #temp main_score
scoreboard players operation #t.x main_score /= #1000 const

scoreboard players operation #t.y main_score *= #n2.x main_score
scoreboard players operation #temp main_score = #t.z main_score
scoreboard players operation #temp main_score *= #n2.z main_score
scoreboard players operation #t.y main_score -= #temp main_score
scoreboard players operation #t.y main_score /= #1000 const

scoreboard players operation #t.z main_score *= #n2.y main_score
scoreboard players operation #temp main_score = #col_nz_y main_score
scoreboard players operation #temp main_score *= #n2.x main_score
scoreboard players operation #t.z main_score -= #temp main_score
scoreboard players operation #t.z main_score /= #1000 const



function physical_vehicles:zzz/vehicle/collision/i_and_r