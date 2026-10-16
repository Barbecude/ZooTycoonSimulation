execute if entity @s[gamemode=survival] run return run scoreboard players set #gamemode main_score 0
execute if entity @s[gamemode=creative] run return run scoreboard players set #gamemode main_score 1
execute if entity @s[gamemode=adventure] run return run scoreboard players set #gamemode main_score 2
scoreboard players set #gamemode main_score 3
