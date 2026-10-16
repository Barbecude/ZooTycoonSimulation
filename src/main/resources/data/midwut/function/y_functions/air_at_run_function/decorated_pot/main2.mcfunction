tp ~ ~1 ~

scoreboard players set #check.block main_score 1
execute align xyz positioned ~0.0625 ~ ~0.0625 if entity @s[dx=0] positioned ~-0.125 ~ ~-0.125 if entity @s[dx=0] run return 0
execute align xyz positioned ~0.25 ~1 ~0.25 if entity @s[dx=0] positioned ~-0.75 ~-0.9375 ~-0.75 if entity @s[dx=0] run return 0
execute align xyz positioned ~0.1875 ~1.0625 ~0.1875 if entity @s[dx=0] positioned ~-0.5 ~-0.8125 ~-0.5 if entity @s[dx=0] run return 0

execute if score #check.air main_score matches 0 align xyz run setblock ~ ~ ~ minecraft:air destroy







