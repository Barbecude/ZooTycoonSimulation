
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

#tellraw @a [{"score":{name:"#x3",objective:"main_score"}}]

scoreboard players operation #x3 main_score /= @s phys_veh.moment_of_inertia_no_mass.x
scoreboard players operation #y3 main_score /= @s phys_veh.moment_of_inertia_no_mass.y
scoreboard players operation #z3 main_score /= @s phys_veh.moment_of_inertia_no_mass.z

#tellraw @a [{score:{name:"#x3",objective:"main_score"}},"  ",{score:{name:"#y3",objective:"main_score"}},"  ",{score:{name:"#z3",objective:"main_score"}}]

scoreboard players operation @s phys_veh.spin.x += #x3 main_score
scoreboard players operation @s phys_veh.spin.y += #y3 main_score
scoreboard players operation @s phys_veh.spin.z += #z3 main_score
