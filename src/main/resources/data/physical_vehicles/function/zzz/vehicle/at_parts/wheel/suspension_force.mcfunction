scoreboard players operation #force main_score = @s phys_veh.wheel.suspension.force
scoreboard players operation #force main_score /= #main.mass main_score
scoreboard players operation #x2 main_score = #force main_score
scoreboard players operation #x2 main_score *= #norm.x main_score
scoreboard players operation #x2 main_score /= #1000 const

scoreboard players operation #y2 main_score = #force main_score
scoreboard players operation #y2 main_score *= #norm.y main_score
scoreboard players operation #y2 main_score /= #1000 const

scoreboard players operation #z2 main_score = #force main_score
scoreboard players operation #z2 main_score *= #norm.z main_score
scoreboard players operation #z2 main_score /= #1000 const
scoreboard players operation #dv.x.x main_score += #x2 main_score
scoreboard players operation #dv.x.y main_score += #y2 main_score
scoreboard players operation #dv.x.z main_score += #z2 main_score


scoreboard players operation #x3 main_score = @s phys_veh.offset2.y
scoreboard players operation #x3 main_score *= #z2 main_score
scoreboard players operation #temp main_score = @s phys_veh.offset2.z
scoreboard players operation #temp main_score *= #y2 main_score
scoreboard players operation #x3 main_score -= #temp main_score
scoreboard players operation #x3 main_score /= #1000 const

scoreboard players operation #y3 main_score = @s phys_veh.offset2.x
scoreboard players operation #y3 main_score *= #z2 main_score
scoreboard players operation #temp main_score = @s phys_veh.offset2.z
scoreboard players operation #temp main_score *= #x2 main_score
scoreboard players operation #y3 main_score -= #temp main_score
scoreboard players operation #y3 main_score /= #1000 const

scoreboard players operation #z3 main_score = @s phys_veh.offset2.x
scoreboard players operation #z3 main_score *= #y2 main_score
scoreboard players operation #temp main_score = @s phys_veh.offset2.y
scoreboard players operation #temp main_score *= #x2 main_score
scoreboard players operation #z3 main_score -= #temp main_score
scoreboard players operation #z3 main_score /= #1000 const

scoreboard players operation #dw.x.x main_score += #x3 main_score
scoreboard players operation #dw.x.y main_score -= #y3 main_score
scoreboard players operation #dw.x.z main_score += #z3 main_score
