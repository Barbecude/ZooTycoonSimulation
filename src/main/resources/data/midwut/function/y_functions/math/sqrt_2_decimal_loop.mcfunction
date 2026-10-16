## (out + num/out) / 2 = out2

scoreboard players add #midwut.temp2 main_score 1

### b = num/a
scoreboard players operation #midwut.temp3 main_score = #midwut.temp main_score
scoreboard players operation #midwut.temp3 main_score /= #out main_score

### a = (a+b)/2
scoreboard players operation #out main_score += #midwut.temp3 main_score
scoreboard players operation #out main_score /= #2 const


scoreboard players operation #midwut.temp3 main_score = #out main_score
scoreboard players operation #midwut.temp3 main_score /= #100 const
scoreboard players add #midwut.temp3 main_score 1
scoreboard players operation #midwut.temp3 main_score *= #midwut.temp3 main_score
scoreboard players operation #midwut.temp3 main_score /= #10000 const

execute unless score #midwut.temp2 main_score matches 20.. unless score #midwut.temp main_score = #midwut.temp3 main_score run function midwut:y_functions/math/sqrt_2_decimal_loop