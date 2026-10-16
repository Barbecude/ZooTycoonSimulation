execute if predicate midwut:check/input/left run scoreboard players set #a main_score 1
execute if predicate midwut:check/input/right run scoreboard players set #d main_score 1
execute if predicate midwut:check/input/sprint run scoreboard players set #sprint main_score 1
execute if predicate midwut:check/input/backward run scoreboard players set #s main_score 1
execute if predicate midwut:check/input/forward run scoreboard players set #w main_score 1
execute if predicate midwut:check/input/jump run scoreboard players set #jump main_score 1
scoreboard players operation #pedal_deltatime main_score = @s phys_veh.pedal_deltatime