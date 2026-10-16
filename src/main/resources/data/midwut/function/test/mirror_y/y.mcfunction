scoreboard players remove #ty main_score 1

execute store result storage midwut:main macros.y int 1 run scoreboard players remove #y2 main_score 1
scoreboard players operation #tz main_score = #z main_score
function midwut:test/mirror_y/z

scoreboard players remove #y2 main_score 1
execute if score #ty main_score matches 1.. positioned ~ ~1 ~ run function midwut:test/mirror_y/y