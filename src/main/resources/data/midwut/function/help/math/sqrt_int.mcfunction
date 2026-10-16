#> midwut:help/math/sqrt_int

# §6 En
## §e Input Data:
### scoreboard players get #number main_score - The number under the root
## §e Output Data:
### scoreboard players get #out main_score - Integer number

# §6 Ru
## §e Входные данные:
### scoreboard players get #number main_score - Число под корнем
## §e Выходные данные:
### scoreboard players get #out main_score - Целое число

# 
execute if score #number main_score matches 1..3 run return run scoreboard players set #out main_score 1
execute if score #number main_score matches ..0 run return run scoreboard players set #out main_score 0
scoreboard players operation #out main_score = #number main_score
scoreboard players operation #out main_score /= #2 const
scoreboard players set #midwut.temp main_score 0
function midwut:y_functions/math/sqrt_int_loop
return run scoreboard players get #out main_score

