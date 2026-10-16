execute positioned ~0.25 ~0.25 ~0.25 if entity @s[dx=0] positioned ~-0.5 ~-0.875 ~-0.5 if entity @s[dx=0] run return 0
execute positioned ~0.3125 ~0.375 ~0.3125 if entity @s[dx=0] positioned ~-0.625 ~-0.5625 ~-0.625 if entity @s[dx=0] run return 0

execute if block ~ ~ ~ bell[facing=north] positioned ~0.4375 ~0.8125 ~ if entity @s[dx=0] positioned ~-0.875 ~-0.875 ~-0.1875 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ bell[facing=south] positioned ~0.4375 ~0.8125 ~0.1875 if entity @s[dx=0] positioned ~-0.875 ~-0.875 ~ if entity @s[dx=0] run return 0
execute if block ~ ~ ~ bell[facing=west] positioned ~ ~0.8125 ~0.4375 if entity @s[dx=0] positioned ~-0.1875 ~-0.875 ~-0.875 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ bell[facing=east] positioned ~0.1875 ~0.8125 ~0.4375 if entity @s[dx=0] positioned ~ ~-0.875 ~-0.875 if entity @s[dx=0] run return 0
return 1