scoreboard players operation #number main_score = #q2x main_score
scoreboard players operation #number main_score *= #q2x main_score
scoreboard players operation #temp main_score = #q2y main_score
scoreboard players operation #temp main_score *= #q2y main_score
scoreboard players operation #number main_score += #temp main_score
scoreboard players operation #temp main_score = #q2z main_score
scoreboard players operation #temp main_score *= #q2z main_score
scoreboard players operation #number main_score += #temp main_score
scoreboard players operation #temp main_score = #q2w main_score
scoreboard players operation #temp main_score *= #q2w main_score
scoreboard players operation #number main_score += #temp main_score
function midwut:help/math/sqrt_int
scoreboard players operation #q2x main_score *= #10000 const
scoreboard players operation #q2y main_score *= #10000 const
scoreboard players operation #q2z main_score *= #10000 const
scoreboard players operation #q2w main_score *= #10000 const
scoreboard players operation #q2x main_score /= #out main_score
scoreboard players operation #q2y main_score /= #out main_score
scoreboard players operation #q2z main_score /= #out main_score
scoreboard players operation #q2w main_score /= #out main_score

#scoreboard players set @s phys_veh.normalize_timer 0