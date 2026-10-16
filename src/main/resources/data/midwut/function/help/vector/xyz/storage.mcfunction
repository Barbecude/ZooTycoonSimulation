#> midwut:help/vector/xyz/storage

# §6 En
## §e Input data
### Before the function, you must set Rotation to the midwut:main storage in the format of a regular entity Rotation nbt tag
### Example: data modify storage midwut:main Rotation set value [135.0f,13.0f]
## §e Output data
### The output data is a vector of length 10000 with coordinates along three axes XYZ
### scoreboard players get #Vec.AXIS main_score - The number with an accuracy of four decimal places in the range -10000..10000

# §6 Ru
## §e Входные данные
### Перед функцией вы должны установить Rotation в сторейдж midwut:main в формате обычного нбт Rotation сущности.
### Пример: data modify storage midwut:main Rotation set value [135.0f,13.0f]
## §e Выходные данные
### Выходными данными является вектор длиною 10000 с координатами по трём осям XYZ
### scoreboard players get #Vec.AXIS main_score - Число, с точностью четыре знака после запятой в диапазоне -10000..10000

# 
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
