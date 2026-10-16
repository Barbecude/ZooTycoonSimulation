scoreboard players remove @s phys_veh.offset.y 875
scoreboard players remove #offset.y main_score 1925

execute on passengers store result score #a main_score run data get entity @s Rotation[0] 1000
execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #a main_score %= #360000 const
execute store result score #x main_score run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
execute store result score #z main_score run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric

scoreboard players operation #x main_score /= #44 const
scoreboard players operation #z main_score /= #44 const
scoreboard players operation #offset.x main_score -= #x main_score
scoreboard players operation #offset.z main_score += #z main_score
