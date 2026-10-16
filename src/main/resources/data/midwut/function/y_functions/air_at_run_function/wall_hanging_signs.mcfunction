execute unless block ~ ~ ~ #minecraft:wall_hanging_signs[facing=east] unless block ~ ~ ~ #minecraft:wall_hanging_signs[facing=west] positioned ~ ~0.875 ~0.375 if entity @s[dx=0] positioned ~ ~-0.875 ~-0.75 if entity @s[dx=0] run return 0
execute unless block ~ ~ ~ #minecraft:wall_hanging_signs[facing=north] unless block ~ ~ ~ #minecraft:wall_hanging_signs[facing=south] positioned ~0.375 ~0.875 ~ if entity @s[dx=0] positioned ~-0.75 ~-0.875 ~ if entity @s[dx=0] run return 0

return 1