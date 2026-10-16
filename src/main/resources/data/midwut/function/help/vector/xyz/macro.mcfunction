#> midwut:help/vector/xyz/macro

# §6 En
## §e Input data
### Running a function with a compound, necessarily with the values Y and X, storing the angle with an accuracy of three decimal places in the range 0..360000
### Example: function midwut:help/vector/xyz/macro {Y:1000,X:80000}
## §e Output data
### The output data is a vector of length 10000 with coordinates along three axes XYZ
### scoreboard players get #Vec.AXIS main_score - The number with an accuracy of four decimal places in the range -10000..10000

# §6 Ru
## §e Входные данные
### Вызов функции с компаундом, обязательно со значениями Y и X, хранящими угол с точностью три знака после запятой в диапазоне 0..360000
### Пример: function midwut:help/vector/xyz/macro {Y:1000,X:80000}
## §e Выходные данные
### Выходными данными является вектор длиною 10000 с координатами по трём осям XYZ
### scoreboard players get #Vec.AXIS main_score - Число, с точностью четыре знака после запятой в диапазоне -10000..10000

# 
$data modify storage midwut:main trigonometric.in $(X)
execute store result score #Vec.Y main_score run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
execute store result score #Vec.X main_score store result score #Vec.Z main_score run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #Vec.Y main_score *= #-1 const

$data modify storage midwut:main trigonometric.in $(Y)
function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric

scoreboard players operation #Vec.X main_score *= #OUT:sin main_score
scoreboard players operation #Vec.X main_score /= #-10000 const
scoreboard players operation #Vec.Z main_score *= #OUT:cos main_score
scoreboard players operation #Vec.Z main_score /= #10000 const
