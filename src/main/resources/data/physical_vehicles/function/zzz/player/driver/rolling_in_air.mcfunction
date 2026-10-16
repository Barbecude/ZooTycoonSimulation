execute if entity @s[tag=phys_veh.turning_steering_wheel,tag=!phys_veh.rolling_in_air] run return fail
tag @s add phys_veh.rolling_in_air
execute if predicate midwut:check/input/left unless predicate midwut:check/input/right run scoreboard players set #rolling_in_air main_score -1
execute if predicate midwut:check/input/right unless predicate midwut:check/input/left run scoreboard players set #rolling_in_air main_score 1
