# §6 En
## scoreboard players get #number main_score - The number from which the factorial will be taken
## scoreboard players get #out main_score - §e Output number

# §6 Ru
## scoreboard players get #number main_score - Число, из которого будет взят факториал
## scoreboard players get #out main_score - §e Выходное число

# 
scoreboard players set #midwut.temp main_score 1
scoreboard players set #out main_score 1
function midwut:y_functions/math/factorial
return run scoreboard players get #out main_score
