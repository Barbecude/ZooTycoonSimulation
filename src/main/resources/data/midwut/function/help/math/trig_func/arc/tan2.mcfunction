#> midwut:help/math/trig_func/arc/tan2

# §6 En
## §e Input data
### scoreboard players get #IN:arctan2.X main_score - Abscissa length (Negative values are allowed)
### scoreboard players get #IN:arctan2.Y main_score - Ordinate length (Negative values are allowed)
## §e Output data
### scoreboard players get #OUT:arctan2 main_score - Angle, with an accuracy of three decimal places. Range: 0..360000

# §6 Ru
## §e Входные данные
### scoreboard players get #IN:arctan2.X main_score - Длина абсциссы (Допустимы отрицательные значения)
### scoreboard players get #IN:arctan2.Y main_score - Длина ординаты (Допустимы отрицательные значения)
## §e Выходные данные
### scoreboard players get #OUT:arctan2 main_score - Угол, с точностью три знака после зяпятой. Диапазон: 0..360000

# 
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute if score #OUT:arctan2 main_score matches ..-1 run scoreboard players operation #OUT:arctan2 main_score *= #-1 const
execute if score #midwut.temp main_score matches ..-1 run scoreboard players operation #midwut.temp main_score *= #-1 const
execute if score #OUT:arctan2 main_score <= #midwut.temp main_score run return run function midwut:y_functions/math/trigonometric/arctan2/1
return run function midwut:y_functions/math/trigonometric/arctan2/2
