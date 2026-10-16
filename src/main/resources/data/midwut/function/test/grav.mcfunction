scoreboard players remove @s main_score 1

execute if score @s main_score matches ..-1 run attribute @s gravity modifier add physics:release 0.5 add_value
execute if score @s main_score matches ..-1 run return run scoreboard players set @s main_score 20
attribute @s gravity modifier remove physics:release