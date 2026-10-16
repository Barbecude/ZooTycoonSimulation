#> midwut:help/vector/xyz/scores

# §6 En
## §e Input data
### scoreboard players get #Rotation.Y main_score - Angle, with an accuracy of three decimal places. Range: 0..360000
### scoreboard players get #Rotation.X main_score - Angle, with an accuracy of three decimal places. Range: 0..360000
## §e Output data
### The output data is a vector of length 10000 with coordinates along three axes XYZ
### scoreboard players get #Vec.AXIS main_score - The number, with an accuracy of four decimal places.

# §6 Ru
## §e Входные данные
### scoreboard players get #Rotation.Y main_score
### scoreboard players get #Rotation.X main_score
## §e Выходные данные
### Выходными данными является вектор длиною 10000 с координатами по трём осям XYZ
### scoreboard players get #Vec.AXIS main_score - Число, с точностью четыре знака после запятой.

# 
execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #Rotation.X main_score %= #360000 const
execute store result score #Vec.Y main_score run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
execute store result score #Vec.X main_score store result score #Vec.Z main_score run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #Vec.Y main_score *= #-1 const

execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #Rotation.Y main_score %= #360000 const
function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric

scoreboard players operation #Vec.X main_score *= #OUT:sin main_score
scoreboard players operation #Vec.X main_score /= #-10000 const
scoreboard players operation #Vec.Z main_score *= #OUT:cos main_score
scoreboard players operation #Vec.Z main_score /= #10000 const
