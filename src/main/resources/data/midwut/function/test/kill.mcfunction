scoreboard players set @s main_score 5
tag @s add gglol
kill @s
execute if entity @e[tag=gglol] run say lol0
execute as @p if entity @e[tag=gglol] run say lol1
execute if score @s main_score matches 5 run say lol2
execute if entity @s[distance=..1] run say lol3
execute if data entity @s DeathTime run say lol4


