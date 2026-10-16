tp @s ~ ~ ~
execute positioned 0.0 0.0 0.0 unless entity @s[distance=..20] run tp @s ^ ^ ^20

data modify storage midwut:main Pos set from entity @s Pos
execute store result score @e[type=minecraft:text_display,tag=phys_veh.main,tag=phys_veh.selected_part,limit=1] phys_veh.velocity.x run data get storage midwut:main Pos[0] 1000
execute store result score @e[type=minecraft:text_display,tag=phys_veh.main,tag=phys_veh.selected_part,limit=1] phys_veh.velocity.y run data get storage midwut:main Pos[1] 1000
execute store result score @e[type=minecraft:text_display,tag=phys_veh.main,tag=phys_veh.selected_part,limit=1] phys_veh.velocity.z run data get storage midwut:main Pos[2] 1000

tp @s 0.0 0.0 0.0