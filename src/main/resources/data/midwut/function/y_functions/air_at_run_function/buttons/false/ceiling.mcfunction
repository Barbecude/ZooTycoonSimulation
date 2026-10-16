execute unless block ~ ~ ~ #minecraft:buttons[facing=north] unless block ~ ~ ~ #minecraft:buttons[facing=south] positioned ~-0.375 ~ ~-0.3125 if entity @s[dx=0] positioned ~0.75 ~0.875 ~0.625 if entity @s[dx=0] run return 0
execute positioned ~-0.3125 ~ ~-0.375 if entity @s[dx=0] positioned ~0.625 ~0.875 ~0.75 run return 0
return 1
