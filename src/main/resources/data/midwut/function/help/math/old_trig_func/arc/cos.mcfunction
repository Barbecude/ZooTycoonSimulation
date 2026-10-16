## scoreboard players get #IN:angle main_score - Input
## scoreboard players get #OUT:arccos main_score - Output

scoreboard players operation #x main_score = #IN:angle main_score

scoreboard players set #sign main_score 0
execute if score #x main_score matches ..-1 run scoreboard players set #sign main_score 1
execute if score #x main_score matches ..-1 run scoreboard players operation #x main_score *= #-1 const

scoreboard players set #number main_score 10000
scoreboard players operation #number main_score -= #x main_score
function midwut:help/math/sqrt_int

scoreboard players set #OUT:arccos main_score -1873
scoreboard players operation #OUT:arccos main_score *= #x main_score
scoreboard players operation #OUT:arccos main_score /= #10000 const
scoreboard players add #OUT:arccos main_score 7426
scoreboard players operation #OUT:arccos main_score *= #x main_score
scoreboard players operation #OUT:arccos main_score /= #10000 const
scoreboard players remove #OUT:arccos main_score 21211
scoreboard players operation #OUT:arccos main_score *= #x main_score
scoreboard players operation #OUT:arccos main_score /= #10000 const
scoreboard players add #OUT:arccos main_score 157072

scoreboard players operation #OUT:arccos main_score *= #out main_score
scoreboard players operation #OUT:arccos main_score /= #100 const

scoreboard players operation #temp main_score = #OUT:arccos main_score
scoreboard players operation #temp main_score *= #2 const
scoreboard players operation #temp main_score *= #sign main_score

scoreboard players operation #OUT:arccos main_score -= #temp main_score

scoreboard players set #temp main_score 31415926
scoreboard players operation #temp main_score *= #sign main_score
scoreboard players operation #OUT:arccos main_score += #temp main_score
scoreboard players operation #OUT:arccos main_score /= #10 const

scoreboard players operation #OUT:arccos:rad main_score = #OUT:arccos main_score

scoreboard players operation #OUT:arccos main_score *= #180 const
scoreboard players operation #OUT:arccos main_score /= #pi_3 const
execute if score #OUT:arccos main_score matches 180001.. run scoreboard players set #OUT:arccos main_score 180000
