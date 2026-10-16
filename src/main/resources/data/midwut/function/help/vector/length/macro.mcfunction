#> midwut:help/vector/length/macro

# §6 En
## §e Input data
### Running a function with a compound, necessarily with the values X, Y and Z, storing the dimension lengths of the vector
### Example: function midwut:help/vector/xyz/macro {Y:1000,X:80000}
## §e Output data
### scoreboard players get #Vec.Length main_score - Vector length

# §6 Ru
## §e Входные данные
### Вызов функции с компаундом, обязательно со значениями X, Y and Z, хранящими длины измерений вектора
### Пример: function midwut:help/vector/xyz/macro {Y:1000,X:80000}
## §e Выходные данные
### scoreboard players get #Vec.Length main_score - Длина вектора

# 
$scoreboard players set #IN:arctan2.X main_score $(Z)
$scoreboard players set #IN:arctan2.Y main_score $(X)
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

$scoreboard players set #IN:arctan2.Y main_score $(Y)
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

return run scoreboard players operation #Vec.Length main_score = #IN:arctan2.X main_score

