execute if block ~ ~ ~ minecraft:lever[facing=north] positioned ~-0.3125 ~-0.25 ~ if entity @s[dx=0] positioned ~0.625 ~0.5 ~0.625 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ minecraft:lever[facing=west] positioned ~ ~-0.25 ~-0.3125 if entity @s[dx=0] positioned ~0.625 ~0.5 ~0.625 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ minecraft:lever[facing=south] positioned ~0.3125 ~-0.25 ~ if entity @s[dx=0] positioned ~-0.625 ~0.5 ~-0.625 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ minecraft:lever[facing=east] positioned ~ ~-0.25 ~0.3125 if entity @s[dx=0] positioned ~-0.625 ~0.5 ~-0.625 if entity @s[dx=0] run return 0
return 1