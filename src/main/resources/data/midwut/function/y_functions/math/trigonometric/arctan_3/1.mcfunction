scoreboard players set #midwut.temp main_score 10000000
execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #midwut.temp main_score /= #IN:tan main_score
execute store result score #OUT:arctan main_score run function midwut:y_functions/math/trigonometric/arctan/macro-1 with storage midwut:main trigonometric
return run scoreboard players add #OUT:arctan main_score 90000