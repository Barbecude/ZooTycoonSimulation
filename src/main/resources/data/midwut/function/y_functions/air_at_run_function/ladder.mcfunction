execute if block ~ ~ ~ minecraft:ladder[facing=north] run return run execute positioned ~-0.01 ~-0.01 ~-0.1875 if entity @s[dx=0.1,dy=0.1]
execute if block ~ ~ ~ minecraft:ladder[facing=east] run return run execute positioned ~0.1875 ~-0.01 ~-0.01 if entity @s[dy=0.1,dz=0.1]
execute if block ~ ~ ~ minecraft:ladder[facing=south] run return run execute positioned ~-0.01 ~-0.01 ~0.1875 if entity @s[dx=0.1,dy=0.1]
return run execute positioned ~-0.1875 ~-0.01 ~-0.01 if entity @s[dy=0.1,dz=0.1]