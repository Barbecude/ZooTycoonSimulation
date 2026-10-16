#> midwut:help/wasd/scores

# §6 En
## §e Input data
### scoreboard players get #IN:arctan2.Y main_score — motion on the X axis
### scoreboard players get #IN:arctan2.X main_score — motion on the Z axis
### scoreboard players get #Rotation.Y main_score — rotation around the Y axis,§c necessarily from the previous tick!
## §e Output data:
### scoreboard players get #WASD.W main_score (0 — if the W button is not pressed, 1 — if the W button is pressed)
### scoreboard players get #WASD.A main_score (0 — if the A button is not pressed, 1 — if the A button is pressed)
### scoreboard players get #WASD.S main_score (0 — if the S button is not pressed, 1 — if the S button is pressed)
### scoreboard players get #WASD.D main_score (0 — if the D button is not pressed, 1 — if the D button is pressed)
### scoreboard players get #WASD.All main_score (0 — if any button is not pressed, 1 — if any button is pressed)
### scoreboard players get #Rotation.Y main_score (The rotation in which the movement is directed, with an accuracy of three decimal places. Range: 0..360000)

# §6 Ru
## §e Входные данные
### scoreboard players get #IN:arctan2.Y main_score — моушен по оси X
### scoreboard players get #IN:arctan2.X main_score — моушен по оси Z
### scoreboard players get #Rotation.Y main_score — поворот вокруг оси Y,§c обязательно с прошлого тика!
## §e Выходные данные:
### scoreboard players get #WASD.W main_score (0 — если кнопка W не зажата, 1 — если кнопка W зажата)
### scoreboard players get #WASD.A main_score (0 — если кнопка A не зажата, 1 — если кнопка A зажата)
### scoreboard players get #WASD.S main_score (0 — если кнопка S не зажата, 1 — если кнопка S зажата)
### scoreboard players get #WASD.D main_score (0 — если кнопка S не зажата, 1 — если кнопка S зажата)
### scoreboard players get #WASD.All main_score (0 — если любая кнопка не зажата, 1 — если какая-нибудь кнопка зажата)
### scoreboard players get #Rotation.Y main_score (Вращение, в котором направлено движение, с точностью три знака после запятой. Диапазон: 0..360000)

# 
function midwut:y_functions/wasd/test
