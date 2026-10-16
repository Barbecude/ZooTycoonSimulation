particle minecraft:dust{color:[0f,1f,0f],scale:2f}
scoreboard players remove @s teleport_distance 1
execute unless block ^ ^ ^0.1 #minecraft:air run scoreboard players set @s teleport_distance 0
execute if score @s teleport_distance matches 0 run return run tp @s ~ ~ ~
execute positioned ^ ^ ^0.1 run function midwut:test/raycast_delete/l