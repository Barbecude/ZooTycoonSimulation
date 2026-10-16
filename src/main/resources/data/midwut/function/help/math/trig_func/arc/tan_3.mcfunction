#> midwut:help/math/trig_func/arc/tan

# §6 En
## I advise you to use arctan2
## §e Input data
### scoreboard players get #IN:tan main_score - The number, with an accuracy of three decimal places. Range: 0..1000
## §e Output data
### scoreboard players get #OUT:arctan main_score - Angle, with an accuracy of three decimal places. Range: 0..45000

# §6 Ru
## Советую использовать arctan2
## §e Входные данные
### scoreboard players get #IN:tan main_score - Число, с точностью три знака после запятой. Диапазон: 0..1000
## §e Выходные данные
### scoreboard players get #OUT:arctan main_score - Угол, с точностью три знака после зяпятой. Диапазон: 0..45000

# 
execute if score #IN:tan main_score matches 0..1000 run return run function midwut:y_functions/math/trigonometric/arctan_3/0
execute if score #IN:tan main_score matches 1001.. run return run function midwut:y_functions/math/trigonometric/arctan_3/1
execute if score #IN:tan main_score matches -1000..0 run return run function midwut:y_functions/math/trigonometric/arctan_3/2
return run function midwut:y_functions/math/trigonometric/arctan_3/3
