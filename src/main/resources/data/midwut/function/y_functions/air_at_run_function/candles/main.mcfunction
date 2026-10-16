execute if block ~ ~ ~ #minecraft:candles[candles=1] positioned ~0.4375 ~-0.0001 ~0.4375 if entity @s[dx=0] positioned ~-0.875 ~-0.625 ~-0.875 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ #minecraft:candles[candles=2] positioned ~0.3125 ~-0.0001 ~0.375 if entity @s[dx=0] positioned ~-0.625 ~-0.625 ~-0.8125 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ #minecraft:candles[candles=3] positioned ~0.3125 ~-0.0001 ~0.375 if entity @s[dx=0] positioned ~-0.6875 ~-0.625 ~-0.6875 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ #minecraft:candles[candles=4] positioned ~0.3125 ~-0.0001 ~0.3125 if entity @s[dx=0] positioned ~-0.6875 ~-0.625 ~-0.625 if entity @s[dx=0] run return 0
return 1