execute if block ~ ~ ~ #minecraft:walls[up=true] positioned ~0.25 ~-0.0001 ~0.25 if entity @s[dx=0,dy=0.1,dz=0] positioned ~-0.5 ~ ~-0.5 if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:walls[east=low] positioned ~0.3125 ~-0.0001 ~0.3125 if entity @s[dx=0,dy=0.1,dz=0] positioned ~ ~-0.125 ~-0.625 if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:walls[east=tall] positioned ~0.3125 ~-0.0001 ~0.3125 if entity @s[dx=0,dy=0.1,dz=0] positioned ~ ~ ~-0.625 if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:walls[west=low] positioned ~ ~-0.0001 ~0.3125 if entity @s[dx=0,dy=0.1,dz=0] positioned ~-0.3125 ~-0.125 ~-0.625 if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:walls[west=tall] positioned ~ ~-0.0001 ~0.3125 if entity @s[dx=0,dy=0.1,dz=0] positioned ~-0.3125 ~ ~-0.625 if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:walls[south=low] positioned ~0.3125 ~-0.0001 ~0.3125 if entity @s[dx=0,dy=0.1,dz=0] positioned ~-0.625 ~-0.125 ~ if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:walls[south=tall] positioned ~0.3125 ~-0.0001 ~0.3125 if entity @s[dx=0,dy=0.1,dz=0] positioned ~-0.625 ~ ~ if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:walls[north=low] positioned ~0.3125 ~-0.0001 ~ if entity @s[dx=0,dy=0.1,dz=0] positioned ~-0.625 ~-0.125 ~-0.3125 if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:walls[north=tall] positioned ~0.3125 ~-0.0001 ~ if entity @s[dx=0,dy=0.1,dz=0] positioned ~-0.625 ~ ~-0.3125 if entity @s[dx=0,dy=0.1,dz=0] run return 0
return 1

