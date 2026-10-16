# §6 En
## Newton's method(root search), a 4-repeat approach
## §e Input Data:
### scoreboard players get #number main_score - The number under the root, §c no more then 214748
## §e Output Data:
### scoreboard players get #out main_score - The number, with an accuracy of two decimal places

# §6 Ru
## Метод Ньютона(поиск корня), подход с 4 повторами
## §e Входные данные:
### scoreboard players get #number main_score - Число под корнем, §c не больше чем 214748
## §e Выходные данные:
### scoreboard players get #out main_score - Число, с точность два знака после запятой

# 
execute if score #number main_score matches 214749.. run return run scoreboard players set #out main_score 0
execute if score #number main_score matches 1..3 run return run scoreboard players set #out main_score 1
execute if score #number main_score matches ..0 run return run scoreboard players set #out main_score 0

scoreboard players operation #midwut.temp main_score = #number main_score
scoreboard players operation #midwut.temp main_score *= #10000 const
scoreboard players set #out main_score 10000
scoreboard players set #midwut.temp2 main_score 0
function midwut:y_functions/math/sqrt_2_decimal_loop

execute if score #out main_score matches ..-1 run scoreboard players operation #out main_score *= #-1 const

return run scoreboard players get #out main_score
