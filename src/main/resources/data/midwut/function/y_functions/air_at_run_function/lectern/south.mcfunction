execute if entity @s[dx=0] positioned ~ ~-0.875 ~ if entity @s[dx=0] run return 0
execute positioned ~0.25 ~0.125 ~0.25 if entity @s[dx=0] positioned ~-0.5 ~-0.25 ~-0.5 if entity @s[dx=0] run return 0

execute positioned ~ ~0.625 ~0.65625 if entity @s[dx=0] positioned ~ ~-0.75 ~-0.71875 if entity @s[dx=0] run return 0
execute positioned ~ ~0.75 ~0.40625 if entity @s[dx=0] positioned ~ ~-0.75 ~-0.75 if entity @s[dx=0] run return 0
execute positioned ~ ~0.875 ~0.125 if entity @s[dx=0] positioned ~ ~-0.75 ~-0.71875 if entity @s[dx=0] run return 0

return 1