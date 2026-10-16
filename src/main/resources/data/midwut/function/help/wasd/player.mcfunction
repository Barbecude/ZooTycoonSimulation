#> midwut:help/wasd/player

# §6 En
## §e Input data
### The player from whom the function is executed
### scoreboard players get #Rotation.Y main_score — rotation around the Y axis,§c necessarily from the last tick!
## §e Output data:
### scoreboard players get #WASD.W main_score (0 — if the W button is not pressed, 1 — if the W button is pressed)
### scoreboard players get #WASD.A main_score (0 — if the A button is not pressed, 1 — if the A button is pressed)
### scoreboard players get #WASD.S main_score (0 — if the S button is not pressed, 1 — if the S button is pressed)
### scoreboard players get #WASD.D main_score (0 — if the D button is not pressed, 1 — if the D button is pressed)
### scoreboard players get #WASD.All main_score (0 — if any button is not pressed, 1 — if any button is pressed)
### scoreboard players get #Rotation.Y main_score (The rotation in which the movement is directed, with an accuracy of three decimal places. Range: 0..360000)

# §6 Ru
## §e Входные данные
### Игрок, от которого исполняется функция
### scoreboard players get #Rotation.Y main_score — поворот вокруг оси Y,§c обязательно с прошлого тика!
## §e Выходные данные:
### scoreboard players get #WASD.W main_score (0 — если кнопка W не зажата, 1 — если кнопка W зажата)
### scoreboard players get #WASD.A main_score (0 — если кнопка A не зажата, 1 — если кнопка A зажата)
### scoreboard players get #WASD.S main_score (0 — если кнопка S не зажата, 1 — если кнопка S зажата)
### scoreboard players get #WASD.D main_score (0 — если кнопка S не зажата, 1 — если кнопка S зажата)
### scoreboard players get #WASD.All main_score (0 — если любая кнопка не зажата, 1 — если какая-нибудь кнопка зажата)
### scoreboard players get #Rotation.Y main_score (Вращение, в котором направлено движение, с точностью три знака после запятой. Диапазон: 0..360000)

# 
data modify storage midwut:main Motion set from entity @s Motion
execute store result score #IN:arctan2.Y main_score run data get storage midwut:main Motion[0] 10000.0
execute store result score #IN:arctan2.X main_score run data get storage midwut:main Motion[2] 10000.0
function midwut:y_functions/wasd/test
