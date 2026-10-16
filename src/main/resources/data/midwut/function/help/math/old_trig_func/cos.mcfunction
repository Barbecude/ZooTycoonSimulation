## scoreboard players get #IN:angle main_score - Input
## scoreboard players get #OUT:cos main_score - Output

## function midwut:help/math/trig_func/cos

scoreboard players operation #midwut.temp main_score = #IN:angle main_score
scoreboard players add #midwut.temp main_score 90000
scoreboard players operation #midwut.temp main_score %= #180000 const
scoreboard players set #midwut.temp2 main_score 180000
scoreboard players operation #midwut.temp2 main_score -= #midwut.temp main_score
scoreboard players operation #midwut.temp2 main_score /= #100 const
scoreboard players operation #midwut.temp2 main_score *= #midwut.temp main_score
scoreboard players operation #OUT:cos main_score = #midwut.temp2 main_score
scoreboard players operation #OUT:cos main_score *= #4 const
scoreboard players operation #midwut.temp2 main_score /= #10 const
scoreboard players set #midwut.temp3 main_score 40500000
scoreboard players operation #midwut.temp3 main_score -= #midwut.temp2 main_score
scoreboard players operation #midwut.temp3 main_score /= #1000 const
scoreboard players operation #OUT:cos main_score /= #midwut.temp3 main_score
execute if score #IN:angle main_score matches 90001..269999 run scoreboard players operation #OUT:cos main_score *= #-1 const

