scoreboard players add #midwut.temp main_score 1
scoreboard players operation #out main_score *= #midwut.temp main_score
execute if score #midwut.temp main_score < #number main_score run function midwut:y_functions/math/factorial