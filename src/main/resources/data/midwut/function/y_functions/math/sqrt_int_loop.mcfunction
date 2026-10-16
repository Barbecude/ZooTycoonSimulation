scoreboard players operation #midwut.temp2 main_score = #out main_score
scoreboard players operation #midwut.temp3 main_score = #number main_score
scoreboard players operation #midwut.temp3 main_score /= #out main_score
scoreboard players operation #out main_score += #midwut.temp3 main_score
scoreboard players operation #out main_score /= #2 const

scoreboard players add #midwut.temp main_score 1
execute unless score #out main_score = #midwut.temp2 main_score if score #midwut.temp main_score matches ..15 run function midwut:y_functions/math/sqrt_int_loop

