execute positioned ~0.125 ~0.6874 ~0.125 if entity @s[dx=0] positioned ~-0.25 ~ ~-0.25 if entity @s[dx=0] run return 1
execute positioned ~-0.01 ~-0.375 ~-0.01 unless entity @s[dx=0.02,dz=0.02] run return 0
execute positioned ~0.375 ~-0.01 ~0.375 if entity @s[dx=0] positioned ~-0.75 ~ ~-0.75 if entity @s[dx=0] run return 0
execute positioned ~0.25 ~0.25 ~0.25 if entity @s[dx=0] positioned ~-0.5 ~ ~-0.5 if entity @s[dx=0] run return 0
return 1