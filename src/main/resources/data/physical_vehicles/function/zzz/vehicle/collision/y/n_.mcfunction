scoreboard players operation #col_ny_x main_score /= #col_ny_c main_score
scoreboard players operation #col_ny_y main_score /= #col_ny_c main_score
scoreboard players operation #col_ny_z main_score /= #col_ny_c main_score
scoreboard players operation #col_ny_v main_score /= #col_ny_c main_score
scoreboard players remove #col_ny_v main_score 50


scoreboard players set #n.x main_score 0
scoreboard players set #n.y main_score 1000
scoreboard players set #n.z main_score 0

scoreboard players set #j main_score 1000
scoreboard players operation #j main_score += @s phys_veh.coeff_e
scoreboard players operation #j main_score *= #col_ny_v main_score
scoreboard players operation #j main_score *= #10 const


scoreboard players set #j_ main_score 1000000
scoreboard players operation #j_ main_score /= @s phys_veh.mass


scoreboard players operation #t.z main_score = #col_ny_x main_score
scoreboard players operation #t.x main_score = #col_ny_y main_score
scoreboard players operation #t.y main_score = #col_ny_z main_score

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
scoreboard players operation #temp main_score = #col_ny_y main_score
scoreboard players operation #temp main_score *= #n.x main_score
scoreboard players operation #t.z main_score -= #temp main_score
scoreboard players operation #t.z main_score /= #1000 const


scoreboard players operation #x3 main_score = #i00 main_score
scoreboard players operation #x3 main_score *= #t.x main_score
scoreboard players operation #tx2 main_score = #i01 main_score
scoreboard players operation #tx2 main_score *= #t.y main_score
scoreboard players operation #tx3 main_score = #i02 main_score
scoreboard players operation #tx3 main_score *= #t.z main_score

scoreboard players operation #y3 main_score = #i10 main_score
scoreboard players operation #y3 main_score *= #t.x main_score
scoreboard players operation #ty2 main_score = #i11 main_score
scoreboard players operation #ty2 main_score *= #t.y main_score
scoreboard players operation #ty3 main_score = #i12 main_score
scoreboard players operation #ty3 main_score *= #t.z main_score

scoreboard players operation #z3 main_score = #i20 main_score
scoreboard players operation #z3 main_score *= #t.x main_score
scoreboard players operation #tz2 main_score = #i21 main_score
scoreboard players operation #tz2 main_score *= #t.y main_score
scoreboard players operation #tz3 main_score = #i22 main_score
scoreboard players operation #tz3 main_score *= #t.z main_score

scoreboard players operation #x3 main_score += #tx2 main_score
scoreboard players operation #x3 main_score += #tx3 main_score

scoreboard players operation #y3 main_score += #ty2 main_score
scoreboard players operation #y3 main_score += #ty3 main_score

scoreboard players operation #z3 main_score += #tz2 main_score
scoreboard players operation #z3 main_score += #tz3 main_score

scoreboard players operation #x3 main_score /= #1000 const
scoreboard players operation #y3 main_score /= #1000 const
scoreboard players operation #z3 main_score /= #1000 const


scoreboard players operation #t.z main_score = #x3 main_score
scoreboard players operation #t.x main_score = #y3 main_score
scoreboard players operation #t.y main_score = #z3 main_score

scoreboard players operation #t.x main_score *= #col_ny_z main_score
scoreboard players operation #temp main_score = #t.y main_score
scoreboard players operation #temp main_score *= #col_ny_y main_score
scoreboard players operation #t.x main_score -= #temp main_score
scoreboard players operation #t.x main_score /= #1000 const

scoreboard players operation #t.y main_score *= #col_ny_x main_score
scoreboard players operation #temp main_score = #t.z main_score
scoreboard players operation #temp main_score *= #col_ny_z main_score
scoreboard players operation #t.y main_score -= #temp main_score
scoreboard players operation #t.y main_score /= #1000 const

scoreboard players operation #t.z main_score *= #col_ny_y main_score
scoreboard players operation #temp main_score = #y3 main_score
scoreboard players operation #temp main_score *= #col_ny_x main_score
scoreboard players operation #t.z main_score -= #temp main_score
scoreboard players operation #t.z main_score /= #1000 const



scoreboard players operation #t.x main_score *= #n.x main_score
scoreboard players operation #t.y main_score *= #n.y main_score
scoreboard players operation #t.z main_score *= #n.z main_score
scoreboard players operation #t.x main_score += #t.y main_score
scoreboard players operation #t.x main_score += #t.z main_score
scoreboard players operation #t.x main_score /= #1000 const
execute if score #t.x main_score matches ..-1 run scoreboard players set #t.x main_score 0
scoreboard players operation #j_ main_score += #t.x main_score



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



scoreboard players operation #t.z main_score = #col_ny_x main_score
scoreboard players operation #t.x main_score = #col_ny_y main_score
scoreboard players operation #t.y main_score = #col_ny_z main_score

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
scoreboard players operation #temp main_score = #col_ny_y main_score
scoreboard players operation #temp main_score *= #n2.x main_score
scoreboard players operation #t.z main_score -= #temp main_score
scoreboard players operation #t.z main_score /= #1000 const



function physical_vehicles:zzz/vehicle/collision/i_and_r