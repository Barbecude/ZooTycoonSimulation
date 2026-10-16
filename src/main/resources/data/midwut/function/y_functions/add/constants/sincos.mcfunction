#> midwut:y_functions/add/constants/sincos
tp ^ ^ ^1
data modify storage midwut:main temp set from entity @s Pos
execute store result storage midwut:main macros.x int -1 run data get storage midwut:main temp[0] 10000
execute store result storage midwut:main macros.z int 1 run data get storage midwut:main temp[2] 10000
function midwut:y_functions/add/constants/sincos_ with storage midwut:main macros

execute if score #temp main_score matches 359999 run return 1
execute store result storage midwut:main macros.a int 1 store result entity @s Rotation[0] float 0.001 run scoreboard players add #temp main_score 1
execute rotated as @s run function midwut:y_functions/add/constants/sincos