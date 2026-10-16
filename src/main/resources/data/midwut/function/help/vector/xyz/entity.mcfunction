#> midwut:help/vector/xyz/entity

# §6 En
## §e Input data
### The entity from which the function is executed
## §e Output data
### The output data is a vector of length 10000 with coordinates along three axes XYZ
### scoreboard players get #Vec.AXIS main_score - The number with an accuracy of four decimal places in the range -10000..10000

# §6 Ru
## §e Входные данные
### Cущности, от которой исполняется функция
## §e Выходные данные
### Выходными данными является вектор длиною 10000 с координатами по трём осям XYZ
### scoreboard players get #Vec.AXIS main_score - Число, с точностью четыре знака после запятой в диапазоне -10000..10000

# 
data modify storage midwut:main Rotation set from entity @s Rotation
execute store result score #Rotation.Y main_score run data get storage midwut:main Rotation[0] 1000
execute store result score #Rotation.X main_score run data get storage midwut:main Rotation[1] 1000

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
