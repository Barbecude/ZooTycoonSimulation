## (scoreboard players get #number main_score) ^ (scoreboard players get #power main_score)

scoreboard players remove #midwut.temp main_score 1

scoreboard players operation #out main_score *= #number main_score

execute if score #midwut.temp main_score matches 2.. run function midwut:y_functions/math/power