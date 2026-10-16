#> midwut:help/motion/looking

# §6 En
## §e Input data
### scoreboard players get #midwut.motion.strength main_score - Strength to launch in looking direction, with an accuracy of three decimal places. (1d = 10000)
## §e Output data
### Player will launch in the looking direction of the command with the desired strength

# §6 Ru
## §e Входные данные
### scoreboard players get #midwut.motion.strength main_score - Сила запуска в направлении взгляда, с точностью три знака после запятой. (1d = 10000)
## §e Выходные данные
### Игрок запустится в направлении, указанном командой, с желаемой силой.

# 
tag @s add midwut.motion.launch
scoreboard players set #midwut.motion main_score 1

execute store result storage midwut:main macros.temp int 1 run scoreboard players get #midwut.motion.strength main_score
execute as 0000051d-0000-0fb7-0000-001500000014 in midwut:main positioned 0.0 0.0 0.0 run function midwut:y_functions/motion/polar_to_xyz with storage midwut:main macros

scoreboard players operation @s midwut.motion.x += #midwut.motion.x main_score
scoreboard players operation @s midwut.motion.y += #midwut.motion.y main_score
scoreboard players operation @s midwut.motion.z += #midwut.motion.z main_score