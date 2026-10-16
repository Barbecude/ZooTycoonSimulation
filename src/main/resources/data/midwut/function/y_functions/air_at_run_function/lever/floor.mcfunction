execute unless block ~ ~ ~ minecraft:lever[facing=north] unless block ~ ~ ~ minecraft:lever[facing=south] positioned ~0.25 ~-0.0001 ~0.3125 if entity @s[dx=0] positioned ~-0.5 ~-0.6249 ~-0.625 if entity @s[dx=0] run return 0
execute positioned ~0.3125 ~-0.0001 ~0.25 if entity @s[dx=0] positioned ~-0.625 ~-0.6249 ~-0.5 if entity @s[dx=0] run return 0
return 1