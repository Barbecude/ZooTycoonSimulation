## scoreboard players get #IN:angle main_score - Input
## scoreboard players get #OUT:arcsin main_score - Output

function midwut:help/math/old_trig_func/arc/cos

scoreboard players set #OUT:arcsin main_score 1570796
scoreboard players operation #OUT:arcsin main_score -= #OUT:arccos:rad main_score

scoreboard players operation #OUT:arcsin:rad main_score = #OUT:arccos main_score

scoreboard players operation #OUT:arcsin main_score *= #180 const
scoreboard players operation #OUT:arcsin main_score /= #pi_3 const
execute if score #OUT:arcsin main_score matches 180001.. run scoreboard players set #OUT:arcsin main_score 180000
