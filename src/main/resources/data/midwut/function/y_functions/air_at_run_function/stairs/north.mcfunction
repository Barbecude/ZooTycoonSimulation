
execute if block ~ ~ ~ #stairs[shape=straight] run return run execute positioned ~ ~ ~0.5 if entity @s[dx=0] run return 1
execute if block ~ ~ ~ #stairs[shape=inner_right] run return run execute positioned ~-0.5 ~ ~0.5 if entity @s[dx=0] run return 1
execute if block ~ ~ ~ #stairs[shape=inner_left] run return run execute positioned ~0.5 ~ ~0.5 if entity @s[dx=0] run return 1

execute if block ~ ~ ~ #stairs[shape=outer_right] run return run execute positioned ~0.5 ~ ~-0.5 if entity @s[dx=0] run return 0
execute if block ~ ~ ~ #stairs[shape=outer_left] run return run execute positioned ~-0.5 ~ ~-0.5 if entity @s[dx=0] run return 0

return 1