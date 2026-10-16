execute if block ~ ~ ~ minecraft:tripwire[north=false,west=false,south=false,east=false] positioned ~0.4375 ~0.0625 ~-0.0001 if entity @s[dz=0.1] positioned ~-0.875 ~-0.875 ~ if entity @s[dz=0.1] run return 0
execute if block ~ ~ ~ minecraft:tripwire[north=true,west=false,south=false,east=false] positioned ~0.4375 ~0.0625 ~-0.25 if entity @s[dx=0] positioned ~-0.875 ~-0.875 ~ if entity @s[dx=0] run return 0
execute if block ~ ~ ~ minecraft:tripwire[south=true,west=false,north=false,east=false] positioned ~0.4375 ~0.0625 ~0.25 if entity @s[dx=0] positioned ~-0.875 ~-0.875 ~ if entity @s[dx=0] run return 0
execute if block ~ ~ ~ minecraft:tripwire[west=true,north=false,south=false,east=false] positioned ~-0.25 ~0.0625 ~0.4375 if entity @s[dx=0] positioned ~ ~-0.875 ~-0.875 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ minecraft:tripwire[east=true,west=false,south=false,north=false] positioned ~0.25 ~0.0625 ~0.4375 if entity @s[dx=0] positioned ~ ~-0.875 ~-0.875 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ minecraft:tripwire[north=true] positioned ~0.4375 ~0.0625 ~-0.5 if entity @s[dx=0] positioned ~-0.875 ~-0.875 ~ if entity @s[dx=0] run return 0
execute if block ~ ~ ~ minecraft:tripwire[south=true] positioned ~0.4375 ~0.0625 ~0.5 if entity @s[dx=0] positioned ~-0.875 ~-0.875 ~ if entity @s[dx=0] run return 0
execute if block ~ ~ ~ minecraft:tripwire[west=true] positioned ~-0.5 ~0.0625 ~0.4375 if entity @s[dx=0] positioned ~ ~-0.875 ~-0.875 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ minecraft:tripwire[east=true] positioned ~0.5 ~0.0625 ~0.4375 if entity @s[dx=0] positioned ~ ~-0.875 ~-0.875 if entity @s[dx=0] run return 0
return 1