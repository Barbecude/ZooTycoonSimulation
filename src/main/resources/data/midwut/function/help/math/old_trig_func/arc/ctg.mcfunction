#==========================#
# made by Antoha256MC #
#==========================#
#
#arcctg = pi/2 - arctg

## scoreboard players get #IN:angle main_score - Input
## scoreboard players get #OUT:arcctg main_score - Output

#scoreboard players operation #test main_score = #IN:angle main_score
#scoreboard players set #IN:angle main_score 10000
#scoreboard players operation #IN:angle main_score /= #test main_score

#function midwut:help/math/trig_func/arctg

#scoreboard players operation #IN:angle main_score = #test main_score
#scoreboard players operation #OUT:arcctg main_score = #OUT:arctg main_score



function midwut:help/math/old_trig_func/arc/tg
scoreboard players set #OUT:arcctg main_score 900
scoreboard players operation #OUT:arcctg main_score -= #OUT:arctg main_score
scoreboard players operation #OUT:arcctg main_score *= #100 const