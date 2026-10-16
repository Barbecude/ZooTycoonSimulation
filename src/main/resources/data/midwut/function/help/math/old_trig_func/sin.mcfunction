## scoreboard players get #IN:angle main_score - Input
## scoreboard players get #OUT:sin main_score - Output

## function midwut:help/math/trig_func/sin

scoreboard players operation #midwut.temp main_score = #IN:angle main_score
scoreboard players operation #midwut.temp main_score %= #180000 const
scoreboard players set #midwut.temp2 main_score 180000
scoreboard players operation #midwut.temp2 main_score -= #midwut.temp main_score
scoreboard players operation #midwut.temp2 main_score /= #100 const
scoreboard players operation #midwut.temp2 main_score *= #midwut.temp main_score
scoreboard players operation #OUT:sin main_score = #midwut.temp2 main_score
scoreboard players operation #OUT:sin main_score *= #4 const
scoreboard players operation #midwut.temp2 main_score /= #10 const
scoreboard players set #midwut.temp3 main_score 40500000
scoreboard players operation #midwut.temp3 main_score -= #midwut.temp2 main_score
scoreboard players operation #midwut.temp3 main_score /= #1000 const
scoreboard players operation #OUT:sin main_score /= #midwut.temp3 main_score
execute if score #IN:angle main_score matches 180001.. run scoreboard players operation #OUT:sin main_score *= #-1 const
#return run scoreboard players get #OUT:sin main_score

