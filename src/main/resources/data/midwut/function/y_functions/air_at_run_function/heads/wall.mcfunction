execute if block ~ ~ ~ #midwut:heads/wall[facing=north] positioned ~0.25 ~0.25 ~0.5 if entity @s[dx=0] positioned ~-0.5 ~-0.5 ~-0.5 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ #midwut:heads/wall[facing=south] positioned ~0.25 ~0.25 ~ if entity @s[dx=0] positioned ~-0.5 ~-0.5 ~-0.5 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ #midwut:heads/wall[facing=west] positioned ~0.5 ~0.25 ~0.25 if entity @s[dx=0] positioned ~-0.5 ~-0.5 ~-0.5 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ #midwut:heads/wall[facing=east] positioned ~ ~0.25 ~0.25 if entity @s[dx=0] positioned ~-0.5 ~-0.5 ~-0.5 if entity @s[dx=0] run return 0
return 1