execute if block ~ ~ ~ #midwut:chest_can_be_double[facing=south] positioned ~0.0625 ~-0.0001 ~0.0625 if entity @s[dx=0] positioned ~ ~-0.1249 ~-0.125 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ #midwut:chest_can_be_double[facing=north] positioned ~ ~-0.0001 ~0.0625 if entity @s[dx=0] positioned ~-0.0625 ~-0.1249 ~-0.125 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ #midwut:chest_can_be_double[facing=east] positioned ~0.0625 ~-0.0001 ~ if entity @s[dx=0] positioned ~-0.125 ~-0.1249 ~-0.0625 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ #midwut:chest_can_be_double[facing=west] positioned ~0.0625 ~-0.0001 ~0.0625 if entity @s[dx=0] positioned ~-0.125 ~-0.1249 ~ if entity @s[dx=0] run return 0
return 1


