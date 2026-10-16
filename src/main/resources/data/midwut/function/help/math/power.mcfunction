# §6 En
## (#number main_score) ^ (#power main_score)
## §e Output number §r: #out main_score

# §6 Ru
## (#number main_score) ^ (#power main_score)
## §e Выходное число §r: #out main_score

# 
execute if score #power main_score matches 0 run return run scoreboard players set #out main_score 1
scoreboard players operation #out main_score = #number main_score
scoreboard players operation #midwut.temp main_score = #power main_score
execute if score #number main_score matches 2.. if score #power main_score matches 2.. run function midwut:y_functions/math/power
return run scoreboard players get #number main_score


