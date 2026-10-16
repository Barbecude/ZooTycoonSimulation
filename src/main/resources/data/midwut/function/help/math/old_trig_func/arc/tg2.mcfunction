#==========================#
# made by Sasai_Kudasai_BM #
#==========================#
#function midwut:help/math/trig_func/arc/tg2


## scoreboard players get #IN:arctg2.X main_score - InputX
## scoreboard players get #IN:arctg2.Y main_score - InputY

## scoreboard players get #OUT:arctg2 main_score - Output


# execute if score #IN:arctg2.X main_score matches 0 run function midwut:y_functions/math/arctg2

scoreboard players operation #IN:angle main_score = #IN:arctg2.Y main_score
scoreboard players operation #IN:angle main_score *= #-100 const
scoreboard players operation #IN:angle main_score /= #IN:arctg2.X main_score

function midwut:help/math/old_trig_func/arc/tg

scoreboard players operation #OUT:arctg2 main_score = #OUT:arctg main_score
execute if score #IN:arctg2.X main_score matches ..-1 run scoreboard players add #OUT:arctg2 main_score 180000
execute if score #OUT:arctg2 main_score matches ..-1 run scoreboard players add #OUT:arctg2 main_score 360000
#execute if score #OUT:arctg2 main_score matches 360001.. run scoreboard players remove #OUT:arctg2 main_score 360000