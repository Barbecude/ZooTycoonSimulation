execute as @e[type=interaction,tag=sit_down] at @s unless entity @e[tag=!sit_down,distance=..2,limit=1] run kill

summon interaction ~ ~-0.15 ~ {Tags:["sit_down","get_id"],width:0f,height:0.15f}
ride @s mount @e[type=interaction,tag=sit_down, tag=get_id,limit=1,distance=..0.151]
tag @e[type=interaction,tag=sit_down, tag=get_id,limit=1,distance=..0.151] remove get_id 