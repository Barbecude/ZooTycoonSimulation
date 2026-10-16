execute if block ~ ~ ~ #minecraft:chains[axis=x] positioned ~-0.01 ~0.40625 ~0.40625 if entity @s[dx=0.1] positioned ~ ~-0.8125 ~-0.8125 if entity @s[dx=0.1] run return 0
execute if block ~ ~ ~ #minecraft:chains[axis=y] positioned ~0.40625 ~-0.01 ~0.40625 if entity @s[dy=0.1] positioned ~-0.8125 ~ ~-0.8125 if entity @s[dy=0.1] run return 0
execute if block ~ ~ ~ #minecraft:chains[axis=z] positioned ~0.40625 ~0.40625 ~-0.01 if entity @s[dz=0.1] positioned ~-0.8125 ~-0.8125 ~ if entity @s[dz=0.1] run return 0

return 1
