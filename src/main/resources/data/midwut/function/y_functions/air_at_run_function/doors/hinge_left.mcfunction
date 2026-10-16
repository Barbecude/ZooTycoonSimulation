execute if block ~ ~ ~ #minecraft:doors[facing=east] positioned ~-0.001 ~-0.001 ~-0.1875 if entity @s[dx=0.1,dy=0.1,dz=0] run return 1
execute if block ~ ~ ~ #minecraft:doors[facing=south] positioned ~0.1875 ~-0.001 ~-0.001 if entity @s[dx=0,dy=0.1,dz=0.1] run return 1
execute if block ~ ~ ~ #minecraft:doors[facing=west] positioned ~-0.001 ~-0.001 ~0.1875 if entity @s[dx=0.1,dy=0.1,dz=0] run return 1
execute if block ~ ~ ~ #minecraft:doors[facing=north] positioned ~-0.1875 ~-0.001 ~-0.001 if entity @s[dx=0,dy=0.1,dz=0.1] run return 1
return 0