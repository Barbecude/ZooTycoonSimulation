# 180000/(25/0.5)
execute store result storage midwut:main trigonometric.in int 3600 run scoreboard players add #timer main_score 1

function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
scoreboard players operation #height main_score += #OUT:sin main_score
execute store result storage midwut:main macros.step double 0.00002519082 run scoreboard players get #height main_score
#tellraw @a [{"score":{"name":"#timer","objective":"main_score"}},"  ",{"score":{"name":"#height","objective":"main_score"}}]

scoreboard players operation #temp main_score = #timer main_score
scoreboard players operation #temp main_score %= #2 const
execute if score #temp main_score matches 1 run function midwut:test/gen_krivaya/rampa0/setblock

function midwut:test/gen_krivaya/rampa0/summon_sin with storage midwut:main macros

# 25 / 0.5
execute unless score #timer main_score matches 50 positioned ~ ~ ~0.5 run function midwut:test/gen_krivaya/rampa0/1
