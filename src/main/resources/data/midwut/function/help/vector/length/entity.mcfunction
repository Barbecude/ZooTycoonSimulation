#> midwut:help/vector/length/entity

# §6 En
## §e Input data:
### The length of the Motion of the entity is calculated. The input data is the entity from which the function is executed.
## §e Output data:
### scoreboard players get #Vec.Length main_score - Vector length

# §6 Ru
## §e Входные данные:
### Вычисляется длина моушена сущности. Входными данными является сущность, от лица которой выполняется функция.
## §e Выходные данные:
### scoreboard players get #Vec.Length main_score - длина вектора

# 
data modify storage midwut:main Motion set from entity @s Motion

execute store result score #IN:arctan2.X main_score run data get storage midwut:main Motion[2] 1000
execute store result score #IN:arctan2.Y main_score run data get storage midwut:main Motion[0] 1000
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

execute store result score #IN:arctan2.Y main_score run data get storage midwut:main Motion[1] 1000
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #Vec.Length main_score = #IN:arctan2.X main_score