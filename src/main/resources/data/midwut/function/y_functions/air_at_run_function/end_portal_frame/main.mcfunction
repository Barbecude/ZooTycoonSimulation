execute positioned ~ ~-0.1875 ~ if entity @s[dx=0] run return 0
execute if block ~ ~ ~ minecraft:end_portal_frame[eye=true] positioned ~0.25 ~0.8124 ~0.25 if entity @s[dx=0.0001] positioned ~-0.5 ~ ~-0.5 if entity @s[dx=0.0001] run return 0
return 1
