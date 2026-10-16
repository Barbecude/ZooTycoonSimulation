scoreboard players add @s phys_veh.coll_count 1

scoreboard players operation #j2 main_score /= @s phys_veh.mass
scoreboard players operation #n2.x main_score *= #j2 main_score
scoreboard players operation #n2.y main_score *= #j2 main_score
scoreboard players operation #n2.z main_score *= #j2 main_score
scoreboard players operation #n2.x main_score /= #-10 const
scoreboard players operation #n2.y main_score /= #-10 const
scoreboard players operation #n2.z main_score /= #-10 const
scoreboard players operation @s phys_veh.coll_velocity.x += #n2.x main_score
scoreboard players operation @s phys_veh.coll_velocity.y += #n2.y main_score
scoreboard players operation @s phys_veh.coll_velocity.z += #n2.z main_score

scoreboard players operation #n2.x main_score /= #10 const
scoreboard players operation #n2.y main_score /= #10 const
scoreboard players operation #n2.z main_score /= #10 const


scoreboard players operation #t.z main_score = #r.x main_score
scoreboard players operation #t.x main_score = #r.y main_score
scoreboard players operation #t.y main_score = #r.z main_score

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
scoreboard players operation #temp main_score = #r.y main_score
scoreboard players operation #temp main_score *= #n2.x main_score
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

scoreboard players operation #x3 main_score /= @s phys_veh.moment_of_inertia_no_mass.x
scoreboard players operation #y3 main_score /= @s phys_veh.moment_of_inertia_no_mass.y
scoreboard players operation #z3 main_score /= @s phys_veh.moment_of_inertia_no_mass.z
#scoreboard players operation #x3 main_score /= #10 const
#scoreboard players operation #y3 main_score /= #10 const
#scoreboard players operation #z3 main_score /= #10 const

scoreboard players operation @s phys_veh.coll_spin.x += #x3 main_score
scoreboard players operation @s phys_veh.coll_spin.y += #y3 main_score
scoreboard players operation @s phys_veh.coll_spin.z += #z3 main_score