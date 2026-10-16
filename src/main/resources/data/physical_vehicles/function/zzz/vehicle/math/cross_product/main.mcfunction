## #x main_score
## #y main_score
## #z main_score

## #x2 main_score
## #y2 main_score
## #z2 main_score

scoreboard players operation #x3 main_score = #y main_score
scoreboard players operation #x3 main_score *= #z2 main_score
scoreboard players operation #temp main_score = #z main_score
scoreboard players operation #temp main_score *= #y2 main_score
scoreboard players operation #x3 main_score -= #temp main_score
scoreboard players operation #x3 main_score /= #1000 const

scoreboard players operation #y3 main_score = #x main_score
scoreboard players operation #y3 main_score *= #z2 main_score
scoreboard players operation #temp main_score = #z main_score
scoreboard players operation #temp main_score *= #x2 main_score
scoreboard players operation #y3 main_score -= #temp main_score
scoreboard players operation #y3 main_score /= #1000 const

scoreboard players operation #z3 main_score = #x main_score
scoreboard players operation #z3 main_score *= #y2 main_score
scoreboard players operation #temp main_score = #y main_score
scoreboard players operation #temp main_score *= #x2 main_score
scoreboard players operation #z3 main_score -= #temp main_score
scoreboard players operation #z3 main_score /= #1000 const
