#> midwut:help/rotate_between_points/get

# §6 En
## §e Input data: §c(Three axes must be specified)
### scoreboard players get #Starting.AXIS main_score
### scoreboard players get #Ending.AXIS main_score
## §e Output data:
### scoreboard players get #Rotation.Y main_score - Angle, with an accuracy of three decimal places. Range: 0..360000
### scoreboard players get #Rotation.X main_score - Angle, with an accuracy of three decimal places. Range: 0..360000

# §6 Ru
## §e Входные данные: §c(Обязательно указывать три оси)
### scoreboard players get #Starting.AXIS main_score
### scoreboard players get #Ending.AXIS main_score
## §e Выходные данные:
### scoreboard players get #Rotation.Y main_score - Угол, с точностью три знака после зяпятой. Диапазон: 0..360000
### scoreboard players get #Rotation.X main_score - Угол, с точностью три знака после зяпятой. Диапазон: 0..360000

# 
scoreboard players operation #IN:arctan2.X main_score = #Ending.Z main_score
scoreboard players operation #IN:arctan2.X main_score -= #Starting.Z main_score
scoreboard players operation #IN:arctan2.Y main_score = #Ending.X main_score
scoreboard players operation #IN:arctan2.Y main_score -= #Starting.X main_score
execute store result score #Rotation.Y main_score run function midwut:help/math/trig_func/arc/tan2

execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #IN:arctan2.Y main_score = #Ending.Y main_score
scoreboard players operation #IN:arctan2.Y main_score -= #Starting.Y main_score
function midwut:help/math/trig_func/arc/tan2
scoreboard players operation #Rotation.X main_score = #OUT:arctan2 main_score






