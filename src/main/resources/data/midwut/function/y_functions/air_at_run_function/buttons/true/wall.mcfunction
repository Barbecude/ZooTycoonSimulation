execute if block ~ ~ ~ #minecraft:buttons[facing=north] positioned ~-0.3125 ~-0.375 ~ if entity @s[dx=0] positioned ~0.625 ~0.75 ~0.9375 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ #minecraft:buttons[facing=west] positioned ~ ~-0.375 ~-0.3125 if entity @s[dx=0] positioned ~0.9375 ~0.75 ~0.625 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ #minecraft:buttons[facing=south] positioned ~0.3125 ~0.375 ~ if entity @s[dx=0] positioned ~-0.625 ~-0.75 ~-0.9375 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ #minecraft:buttons[facing=east] positioned ~ ~0.375 ~0.3125 if entity @s[dx=0] positioned ~-0.9375 ~-0.75 ~-0.625 if entity @s[dx=0] run return 0
return 1
