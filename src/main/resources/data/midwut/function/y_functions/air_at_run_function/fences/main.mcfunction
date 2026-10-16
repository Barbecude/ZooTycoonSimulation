execute if block ~ ~ ~ #minecraft:fences positioned ~0.375 ~-0.0001 ~0.375 if entity @s[dx=0,dy=0.1,dz=0] positioned ~-0.75 ~ ~-0.75 if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:fences[east=true] positioned ~0.375 ~-0.0001 ~0.375 if entity @s[dx=0,dy=0.1,dz=0] positioned ~ ~ ~-0.75 if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:fences[west=true] positioned ~-0.375 ~-0.0001 ~0.375 if entity @s[dx=0,dy=0.1,dz=0] positioned ~ ~ ~-0.75 if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:fences[south=true] positioned ~0.375 ~-0.0001 ~0.375 if entity @s[dx=0,dy=0.1,dz=0] positioned ~-0.75 ~ ~ if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:fences[north=true] positioned ~0.375 ~-0.0001 ~-0.375 if entity @s[dx=0,dy=0.1,dz=0] positioned ~-0.75 ~ ~ if entity @s[dx=0,dy=0.1,dz=0] run return 0
return 1

