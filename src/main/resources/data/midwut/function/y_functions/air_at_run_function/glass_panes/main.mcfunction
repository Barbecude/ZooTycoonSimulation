execute positioned ~0.4375 ~-0.0001 ~0.4375 if entity @s[dx=0,dy=0.1,dz=0] positioned ~-0.875 ~ ~-0.875 if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #midwut:glass/all_panes[east=true] positioned ~0.4375 ~-0.0001 ~0.4375 if entity @s[dx=0,dy=0.1,dz=0] positioned ~ ~ ~-0.875 if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #midwut:glass/all_panes[west=true] positioned ~-0.4375 ~-0.0001 ~0.4375 if entity @s[dx=0,dy=0.1,dz=0] positioned ~ ~ ~-0.875 if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #midwut:glass/all_panes[south=true] positioned ~0.4375 ~-0.0001 ~0.4375 if entity @s[dx=0,dy=0.1,dz=0] positioned ~-0.875 ~ ~ if entity @s[dx=0,dy=0.1,dz=0] run return 0
execute if block ~ ~ ~ #midwut:glass/all_panes[north=true] positioned ~0.4375 ~-0.0001 ~-0.4375 if entity @s[dx=0,dy=0.1,dz=0] positioned ~-0.875 ~ ~ if entity @s[dx=0,dy=0.1,dz=0] run return 0
return 1

