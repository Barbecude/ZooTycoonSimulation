#> midwut:help/motion/xyz

# §6 En
## §e Input data
### scoreboard players get #midwut.motion.x main_score - Strength to launch on the x-axis, with an accuracy of four decimal places. (1d = 10000)
### scoreboard players get #midwut.motion.y main_score - Strength to launch on the y-axis, with an accuracy of four decimal places. (1d = 10000)
### scoreboard players get #midwut.motion.z main_score - Strength to launch on the z-axis, with an accuracy of four decimal places. (1d = 10000)
####§6 If you need to ignore the current motion, add the "midwut.motion.ignore_motion" tag to the player.
## §e Output data
### Player will launch in the specified direction

# §6 Ru
## §e Входные данные
### scoreboard players get #midwut.motion.x main_score - Сила запуска по оси x, с точностью четыре знака после запятой. (1d = 10000)
### scoreboard players get #midwut.motion.y main_score - Сила запуска по оси y, с точностью четыре знака после запятой. (1d = 10000)
### scoreboard players get #midwut.motion.z main_score - Сила запуска по оси z, с точностью четыре знака после запятой. (1d = 10000)
####§6 Если нужно игнорировать текущий моушен, добавьте игроку тег "midwut.motion.ignore_motion"
## §e Выходные данные
### Игрок запустится в указанном направлении.

# 
tag @s add midwut.motion.launch
scoreboard players set #midwut.motion main_score 1

scoreboard players operation @s midwut.motion.x += #midwut.motion.x main_score
scoreboard players operation @s midwut.motion.y += #midwut.motion.y main_score
scoreboard players operation @s midwut.motion.z += #midwut.motion.z main_score

