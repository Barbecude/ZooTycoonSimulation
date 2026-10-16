execute if block ~ ~ ~ minecraft:big_dripleaf[tilt=full] run return 1
execute if block ~ ~ ~ minecraft:big_dripleaf[tilt=partial] positioned ~-0.01 ~0.6875 ~-0.01 if entity @s[dx=0.1,dz=0.1] positioned ~-0.01 ~-0.875 ~-0.01 if entity @s[dx=0.1,dz=0.1] run return 0
execute positioned ~-0.01 ~0.6875 ~-0.01 if entity @s[dx=0.1,dz=0.1] positioned ~-0.01 ~-0.75 ~-0.01 if entity @s[dx=0.1,dz=0.1] run return 0
return 1