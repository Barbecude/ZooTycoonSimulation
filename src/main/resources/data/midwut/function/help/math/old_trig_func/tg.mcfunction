## scoreboard players get #IN:angle main_score - Input
## scoreboard players get #OUT:tg main_score - Output

## function midwut:help/math/trig_func/tg

function midwut:help/math/old_trig_func/sin
function midwut:help/math/old_trig_func/cos

scoreboard players operation #OUT:tg main_score = #OUT:sin main_score
scoreboard players operation #OUT:tg main_score *= #10000 const
scoreboard players operation #OUT:tg main_score /= #OUT:cos main_score