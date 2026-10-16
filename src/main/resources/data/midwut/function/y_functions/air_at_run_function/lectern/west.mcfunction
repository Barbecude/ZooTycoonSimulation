execute if entity @s[dx=0] positioned ~ ~-0.875 ~ if entity @s[dx=0] run return 0
execute positioned ~0.25 ~0.125 ~0.25 if entity @s[dx=0] positioned ~-0.5 ~-0.25 ~-0.5 if entity @s[dx=0] run return 0

execute positioned ~0.0625 ~0.625 ~ if entity @s[dx=0] positioned ~-0.71875 ~-0.75 ~ if entity @s[dx=0] run return 0
execute positioned ~0.34375 ~0.75 ~ if entity @s[dx=0] positioned ~-0.75 ~-0.75 ~ if entity @s[dx=0] run return 0
execute positioned ~0.59375 ~0.875 ~ if entity @s[dx=0] positioned ~-0.71875 ~-0.75 ~ if entity @s[dx=0] run return 0

return 1