execute positioned ~0.4375 ~-0.0001 ~0.4375 if entity @s[dx=0,dy=0.0001,dz=0] positioned ~-0.875 ~ ~-0.875 if entity @s[dx=0,dy=0.0001,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:bars[east=true] positioned ~0.4375 ~-0.0001 ~0.4375 if entity @s[dx=0,dy=0.0001,dz=0] positioned ~ ~ ~-0.875 if entity @s[dx=0,dy=0.0001,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:bars[west=true] positioned ~-0.4375 ~-0.0001 ~0.4375 if entity @s[dx=0,dy=0.0001,dz=0] positioned ~ ~ ~-0.875 if entity @s[dx=0,dy=0.0001,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:bars[south=true] positioned ~0.4375 ~-0.0001 ~0.4375 if entity @s[dx=0,dy=0.0001,dz=0] positioned ~-0.875 ~ ~ if entity @s[dx=0,dy=0.0001,dz=0] run return 0
execute if block ~ ~ ~ #minecraft:bars[north=true] positioned ~0.4375 ~-0.0001 ~-0.4375 if entity @s[dx=0,dy=0.0001,dz=0] positioned ~-0.875 ~ ~ if entity @s[dx=0,dy=0.0001,dz=0] run return 0
return 1

