scoreboard players operation #npx main_score = @s phys_veh.collision_box.px
scoreboard players operation #npx main_score -= #r3.x main_score
scoreboard players operation #nnx main_score = #r3.x main_score
scoreboard players operation #nnx main_score -= @s phys_veh.collision_box.nx
scoreboard players operation #npy main_score = @s phys_veh.collision_box.py
scoreboard players operation #npy main_score -= #r3.y main_score
scoreboard players operation #nny main_score = #r3.y main_score
scoreboard players operation #nny main_score -= @s phys_veh.collision_box.ny
scoreboard players operation #npz main_score = @s phys_veh.collision_box.pz
scoreboard players operation #npz main_score -= #r3.z main_score
scoreboard players operation #nnz main_score = #r3.z main_score
scoreboard players operation #nnz main_score -= @s phys_veh.collision_box.nz
scoreboard players operation #min main_score = #npx main_score
scoreboard players operation #min main_score < #nnx main_score
scoreboard players operation #min main_score < #npy main_score
scoreboard players operation #min main_score < #nny main_score
scoreboard players operation #min main_score < #npz main_score
scoreboard players operation #min main_score < #nnz main_score
function physical_vehicles:zzz/vehicle/collision/dynamic/normal_match/main


scoreboard players operation #r3.x main_score = #n.x main_score
scoreboard players operation #r3.y main_score = #n.y main_score
scoreboard players operation #r3.z main_score = #n.z main_score

scoreboard players operation #n.x main_score = @s phys_veh.m00
scoreboard players operation #n.x main_score *= #r3.x main_score
scoreboard players operation #tx2 main_score = @s phys_veh.m01
scoreboard players operation #tx2 main_score *= #r3.y main_score
scoreboard players operation #tx3 main_score = @s phys_veh.m02
scoreboard players operation #tx3 main_score *= #r3.z main_score

scoreboard players operation #n.y main_score = @s phys_veh.m10
scoreboard players operation #n.y main_score *= #r3.x main_score
scoreboard players operation #ty2 main_score = @s phys_veh.m11
scoreboard players operation #ty2 main_score *= #r3.y main_score
scoreboard players operation #ty3 main_score = @s phys_veh.m12
scoreboard players operation #ty3 main_score *= #r3.z main_score

scoreboard players operation #n.z main_score = @s phys_veh.m20
scoreboard players operation #n.z main_score *= #r3.x main_score
scoreboard players operation #tz2 main_score = @s phys_veh.m21
scoreboard players operation #tz2 main_score *= #r3.y main_score
scoreboard players operation #tz3 main_score = @s phys_veh.m22
scoreboard players operation #tz3 main_score *= #r3.z main_score

scoreboard players operation #n.x main_score += #tx2 main_score
scoreboard players operation #n.x main_score += #tx3 main_score

scoreboard players operation #n.y main_score += #ty2 main_score
scoreboard players operation #n.y main_score += #ty3 main_score

scoreboard players operation #n.z main_score += #tz2 main_score
scoreboard players operation #n.z main_score += #tz3 main_score

scoreboard players operation #n.x main_score /= #10000 const
scoreboard players operation #n.y main_score /= #10000 const
scoreboard players operation #n.z main_score /= #10000 const



scoreboard players operation #vp2.z main_score = @s phys_veh.spin.x
scoreboard players operation #vp2.x main_score = @s phys_veh.spin.y
scoreboard players operation #vp2.y main_score = @s phys_veh.spin.z

scoreboard players operation #vp2.x main_score *= #r2.z main_score
scoreboard players operation #temp main_score = #vp2.y main_score
scoreboard players operation #temp main_score *= #r2.y main_score
scoreboard players operation #vp2.x main_score -= #temp main_score
scoreboard players operation #vp2.x main_score /= #10000 const

scoreboard players operation #vp2.y main_score *= #r2.x main_score
scoreboard players operation #temp main_score = #vp2.z main_score
scoreboard players operation #temp main_score *= #r2.z main_score
scoreboard players operation #vp2.y main_score -= #temp main_score
scoreboard players operation #vp2.y main_score /= #10000 const

scoreboard players operation #vp2.z main_score *= #r2.y main_score
scoreboard players operation #temp main_score = @s phys_veh.spin.y
scoreboard players operation #temp main_score *= #r2.x main_score
scoreboard players operation #vp2.z main_score -= #temp main_score
scoreboard players operation #vp2.z main_score /= #10000 const

scoreboard players operation #vp2.x main_score += @s phys_veh.velocity.x
scoreboard players operation #vp2.y main_score += @s phys_veh.velocity.y
scoreboard players operation #vp2.z main_score += @s phys_veh.velocity.z


scoreboard players operation #vp.x main_score -= #vp2.x main_score
scoreboard players operation #vp.y main_score -= #vp2.y main_score
scoreboard players operation #vp.z main_score -= #vp2.z main_score


scoreboard players operation #vn main_score = #vp.x main_score
scoreboard players operation #vn main_score *= #n.x main_score
scoreboard players operation #temp main_score = #vp.y main_score
scoreboard players operation #temp main_score *= #n.y main_score
scoreboard players operation #vn main_score += #temp main_score
scoreboard players operation #temp main_score = #vp.z main_score
scoreboard players operation #temp main_score *= #n.z main_score
scoreboard players operation #vn main_score += #temp main_score
scoreboard players operation #vn main_score /= #1000 const

execute if score #vn main_score matches 0.. run return fail

scoreboard players operation #min main_score *= #-20 const
scoreboard players operation #vn main_score < #min main_score


scoreboard players set #j main_score 1000
scoreboard players operation #j main_score += @s phys_veh.coeff_e
scoreboard players operation #j main_score *= #vn main_score
scoreboard players operation #j main_score *= #10 const


scoreboard players set #j_ main_score 1000000
scoreboard players operation #j_ main_score /= #main.mass main_score
scoreboard players set #temp main_score 1000000
scoreboard players operation #temp main_score /= @s phys_veh.mass
scoreboard players operation #j_ main_score += #temp main_score




scoreboard players operation #t.z main_score = #r.x main_score
scoreboard players operation #t.x main_score = #r.y main_score
scoreboard players operation #t.y main_score = #r.z main_score

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
scoreboard players operation #temp main_score = #r.y main_score
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

scoreboard players operation #x3 main_score /= #main.moment_of_inertia.x main_score
scoreboard players operation #y3 main_score /= #main.moment_of_inertia.y main_score
scoreboard players operation #z3 main_score /= #main.moment_of_inertia.z main_score



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

scoreboard players operation #x3 main_score *= #r.z main_score
scoreboard players operation #temp main_score = #y3 main_score
scoreboard players operation #temp main_score *= #r.y main_score
scoreboard players operation #x3 main_score -= #temp main_score
scoreboard players operation #x3 main_score /= #1000 const

scoreboard players operation #y3 main_score *= #r.x main_score
scoreboard players operation #temp main_score = #z3 main_score
scoreboard players operation #temp main_score *= #r.z main_score
scoreboard players operation #y3 main_score -= #temp main_score
scoreboard players operation #y3 main_score /= #1000 const

scoreboard players operation #z3 main_score *= #r.y main_score
scoreboard players operation #temp main_score = #t.y main_score
scoreboard players operation #temp main_score *= #r.x main_score
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




scoreboard players operation #t.z main_score = #r2.x main_score
scoreboard players operation #t.x main_score = #r2.y main_score
scoreboard players operation #t.y main_score = #r2.z main_score

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
scoreboard players operation #temp main_score = #r2.y main_score
scoreboard players operation #temp main_score *= #n.x main_score
scoreboard players operation #t.z main_score -= #temp main_score
scoreboard players operation #t.z main_score /= #1000 const



scoreboard players operation #x3 main_score = @s phys_veh.m00_3
scoreboard players operation #x3 main_score *= #t.x main_score
scoreboard players operation #tx2 main_score = @s phys_veh.m10_3
scoreboard players operation #tx2 main_score *= #t.y main_score
scoreboard players operation #tx3 main_score = @s phys_veh.m20_3
scoreboard players operation #tx3 main_score *= #t.z main_score

scoreboard players operation #y3 main_score = @s phys_veh.m01_3
scoreboard players operation #y3 main_score *= #t.x main_score
scoreboard players operation #ty2 main_score = @s phys_veh.m11_3
scoreboard players operation #ty2 main_score *= #t.y main_score
scoreboard players operation #ty3 main_score = @s phys_veh.m21_3
scoreboard players operation #ty3 main_score *= #t.z main_score

scoreboard players operation #z3 main_score = @s phys_veh.m02_3
scoreboard players operation #z3 main_score *= #t.x main_score
scoreboard players operation #tz2 main_score = @s phys_veh.m12_3
scoreboard players operation #tz2 main_score *= #t.y main_score
scoreboard players operation #tz3 main_score = @s phys_veh.m22_3
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



scoreboard players operation #t.x main_score = @s phys_veh.m00_3
scoreboard players operation #t.x main_score *= #x3 main_score
scoreboard players operation #tx2 main_score = @s phys_veh.m01_3
scoreboard players operation #tx2 main_score *= #y3 main_score
scoreboard players operation #tx3 main_score = @s phys_veh.m02_3
scoreboard players operation #tx3 main_score *= #z3 main_score

scoreboard players operation #t.y main_score = @s phys_veh.m10_3
scoreboard players operation #t.y main_score *= #x3 main_score
scoreboard players operation #ty2 main_score = @s phys_veh.m11_3
scoreboard players operation #ty2 main_score *= #y3 main_score
scoreboard players operation #ty3 main_score = @s phys_veh.m12_3
scoreboard players operation #ty3 main_score *= #z3 main_score

scoreboard players operation #t.z main_score = @s phys_veh.m20_3
scoreboard players operation #t.z main_score *= #x3 main_score
scoreboard players operation #tz2 main_score = @s phys_veh.m21_3
scoreboard players operation #tz2 main_score *= #y3 main_score
scoreboard players operation #tz3 main_score = @s phys_veh.m22_3
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

scoreboard players operation #x3 main_score *= #r2.z main_score
scoreboard players operation #temp main_score = #y3 main_score
scoreboard players operation #temp main_score *= #r2.y main_score
scoreboard players operation #x3 main_score -= #temp main_score
scoreboard players operation #x3 main_score /= #1000 const

scoreboard players operation #y3 main_score *= #r2.x main_score
scoreboard players operation #temp main_score = #z3 main_score
scoreboard players operation #temp main_score *= #r2.z main_score
scoreboard players operation #y3 main_score -= #temp main_score
scoreboard players operation #y3 main_score /= #1000 const

scoreboard players operation #z3 main_score *= #r2.y main_score
scoreboard players operation #temp main_score = #t.y main_score
scoreboard players operation #temp main_score *= #r2.x main_score
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



scoreboard players operation #j main_score /= #j_ main_score

scoreboard players operation #j2 main_score = #j main_score
scoreboard players operation #n2.x main_score = #n.x main_score
scoreboard players operation #n2.y main_score = #n.y main_score
scoreboard players operation #n2.z main_score = #n.z main_score
scoreboard players operation #j main_score /= @s phys_veh.mass
scoreboard players operation #n.x main_score *= #j main_score
scoreboard players operation #n.y main_score *= #j main_score
scoreboard players operation #n.z main_score *= #j main_score
scoreboard players operation #n.x main_score /= #-10 const
scoreboard players operation #n.y main_score /= #-10 const
scoreboard players operation #n.z main_score /= #-10 const

scoreboard players operation @s phys_veh.coll_velocity.x -= #n.x main_score
scoreboard players operation @s phys_veh.coll_velocity.y -= #n.y main_score
scoreboard players operation @s phys_veh.coll_velocity.z -= #n.z main_score

scoreboard players operation #n.x main_score /= #10 const
scoreboard players operation #n.y main_score /= #10 const
scoreboard players operation #n.z main_score /= #10 const


scoreboard players operation #t.z main_score = #r2.x main_score
scoreboard players operation #t.x main_score = #r2.y main_score
scoreboard players operation #t.y main_score = #r2.z main_score

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
scoreboard players operation #temp main_score = #r2.y main_score
scoreboard players operation #temp main_score *= #n.x main_score
scoreboard players operation #t.z main_score -= #temp main_score
scoreboard players operation #t.z main_score /= #1000 const



scoreboard players operation #x3 main_score = @s phys_veh.m00_3
scoreboard players operation #x3 main_score *= #t.x main_score
scoreboard players operation #tx2 main_score = @s phys_veh.m10_3
scoreboard players operation #tx2 main_score *= #t.y main_score
scoreboard players operation #tx3 main_score = @s phys_veh.m20_3
scoreboard players operation #tx3 main_score *= #t.z main_score

scoreboard players operation #y3 main_score = @s phys_veh.m01_3
scoreboard players operation #y3 main_score *= #t.x main_score
scoreboard players operation #ty2 main_score = @s phys_veh.m11_3
scoreboard players operation #ty2 main_score *= #t.y main_score
scoreboard players operation #ty3 main_score = @s phys_veh.m21_3
scoreboard players operation #ty3 main_score *= #t.z main_score

scoreboard players operation #z3 main_score = @s phys_veh.m02_3
scoreboard players operation #z3 main_score *= #t.x main_score
scoreboard players operation #tz2 main_score = @s phys_veh.m12_3
scoreboard players operation #tz2 main_score *= #t.y main_score
scoreboard players operation #tz3 main_score = @s phys_veh.m22_3
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

scoreboard players operation @s phys_veh.coll_spin.x -= #x3 main_score
scoreboard players operation @s phys_veh.coll_spin.y -= #y3 main_score
scoreboard players operation @s phys_veh.coll_spin.z -= #z3 main_score

scoreboard players add @s phys_veh.coll_count 1

execute as @e[type=minecraft:text_display,tag=phys_veh.main,tag=phys_veh.selected_part,limit=1,x=0] run function physical_vehicles:zzz/vehicle/collision/dynamic/add_vels