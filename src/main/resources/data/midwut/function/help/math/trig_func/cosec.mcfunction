#> midwut:help/math/trig_func/cosec

# §6 Ru
## §e Input data
### scoreboard players get #IN:angle main_score - Angle, with an accuracy of three decimal places. Range: 0..360000
## §e Output data
### scoreboard players get #OUT:cosec main_score - The number, with an accuracy of four decimal places. Range: -10000..10000

# §6 Ru
## §e Входные данные
### scoreboard players get #IN:angle main_score - Угол, с точностью три знака после зяпятой. Диапазон: 0..360000
## §e Выходные данные
### scoreboard players get #OUT:cosec main_score - Число, с точностью четыре знака после запятой. Диапазон: -10000..10000

# 
function midwut:help/math/trig_func/sin
scoreboard players set #OUT:cosec main_score 100000000
return run scoreboard players operation #OUT:cosec main_score /= #OUT:sin main_score