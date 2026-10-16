execute if block ~ ~ ~ minecraft:tripwire_hook[facing=north] positioned ~-0.3125 ~ ~ if entity @s[dx=0] positioned ~0.625 ~-0.375 ~0.625 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ minecraft:tripwire_hook[facing=west] positioned ~ ~ ~-0.3125 if entity @s[dx=0] positioned ~0.625 ~-0.375 ~0.625 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ minecraft:tripwire_hook[facing=south] positioned ~0.3125 ~ ~ if entity @s[dx=0] positioned ~-0.625 ~-0.375 ~-0.625 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ minecraft:tripwire_hook[facing=east] positioned ~ ~ ~0.3125 if entity @s[dx=0] positioned ~-0.625 ~-0.375 ~-0.625 if entity @s[dx=0] run return 0
return 1
