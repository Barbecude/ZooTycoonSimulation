#> midwut:help/math/trig_func/arc/cos

# §6 En
## §e Input data
### scoreboard players get #IN:cos main_score - The number, with an accuracy of four decimal places. Range: -10000..10000
## §e Output data
### scoreboard players get #OUT:arccos main_score - Angle, with an accuracy of three decimal places. Range: 0..180000

# §6 Ru
## §e Входные данные
### scoreboard players get #IN:cos main_score - Число, с точностью четыре знака после запятой. Диапазон: -10000..10000
## §e Выходные данные
### scoreboard players get #OUT:arccos main_score - Угол, с точностью три знака после зяпятой. Диапазон: 0..180000

# 
execute store result storage midwut:main trigonometric.in int 1 run scoreboard players get #IN:cos main_score
return run execute store result score #OUT:arccos main_score run function midwut:y_functions/math/trigonometric/arccos with storage midwut:main trigonometric