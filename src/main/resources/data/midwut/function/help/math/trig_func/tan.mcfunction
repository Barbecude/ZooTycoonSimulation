#> midwut:help/math/trig_func/tan

# §6 Ru
## §e Input data
### scoreboard players get #IN:angle main_score - Angle, with an accuracy of three decimal places. Range: 0..360000
## §e Output data
### scoreboard players get #OUT:tan main_score - The number, with an accuracy of four decimal places. Range: -10000..10000

# §6 Ru
## §e Входные данные
### scoreboard players get #IN:angle main_score - Угол, с точностью три знака после зяпятой. Диапазон: 0..360000
## §e Выходные данные
### scoreboard players get #OUT:tan main_score - Число, с точностью четыре знака после запятой. Диапазон: -10000..10000

# 
execute store result storage midwut:main trigonometric.in int 1 run scoreboard players get #IN:angle main_score
execute store result score #OUT:tan main_score run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric

scoreboard players operation #OUT:tan main_score *= #10000 const
return run scoreboard players operation #OUT:tan main_score /= #OUT:cos main_score