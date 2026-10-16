#==========================#
# made by Sasai_Kudasai_BM #
#==========================#
# /function midwut:help/math/trig_func/arc/tg
#y=-{631(-143000+10000000/{100+x}+430000000/{10000+x^2})}/100000
#   C1       A              AD        B         BD           C2

## scoreboard players get #IN:angle main_score - Input
## scoreboard players get #OUT:arctg main_score - Output


scoreboard players set #midwut.temp main_score 10000000
scoreboard players set #midwut.temp2 main_score 430000000

scoreboard players operation #X main_score = #IN:angle main_score
execute if score #IN:angle main_score matches ..-1 run scoreboard players operation #X main_score *= #-1 const

scoreboard players operation #AD main_score = #X main_score
scoreboard players add #AD main_score 100
scoreboard players operation #midwut.temp main_score /= #AD main_score

execute store result score #BD main_score run scoreboard players operation #X main_score *= #X main_score
scoreboard players add #BD main_score 10000

scoreboard players operation #midwut.temp2 main_score /= #BD main_score

scoreboard players operation #midwut.temp main_score += #midwut.temp2 main_score
scoreboard players remove #midwut.temp main_score 143000
scoreboard players operation #midwut.temp main_score *= #-631 const
scoreboard players operation #midwut.temp main_score /= #100000 const
scoreboard players operation #OUT:arctg main_score = #midwut.temp main_score

scoreboard players operation #OUT:arctg main_score < #900 const
execute if score #IN:angle main_score matches ..-1 run scoreboard players operation #OUT:arctg main_score *= #-1 const
scoreboard players operation #OUT:arctg main_score *= #100 const