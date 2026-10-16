#> midwut:help/math/trig_func/arc/cotan

# §6 En
## §e Input data
### scoreboard players get #IN:tan main_score - The number, with an accuracy of four decimal places. Range: 0..10000
## §e Output data
### scoreboard players get #OUT:arccotan main_score - Angle, with an accuracy of three decimal places. Range: 45000..90000

# §6 Ru
## §e Входные данные
### scoreboard players get #IN:tan main_score - Число, с точностью четыре знака после запятой. Диапазон: 0..10000
## §e Выходные данные
### scoreboard players get #OUT:arccotan main_score - Угол, с точностью три знака после зяпятой. Диапазон: 45000..90000

# 
function midwut:help/math/trig_func/arc/tan
scoreboard players set #OUT:arccotan main_score 90000
return run scoreboard players operation #OUT:arccotan main_score -= #OUT:arctan main_score