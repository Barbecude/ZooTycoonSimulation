data modify storage midwut:main Rotation set from entity @s Rotation
execute store result score #y main_score run data get storage midwut:main Rotation[0] 1000
execute store result score #x main_score run data get storage midwut:main Rotation[1] 1000
execute store result storage midwut:main Rotation[0] float 0.001 run scoreboard players operation #y main_score += #1000 const
execute store result storage midwut:main Rotation[1] float 0.001 run scoreboard players operation #x main_score += #1000 const

execute summon marker run function midwut:test/llama/set_rotation
execute on vehicle run tag @s add phys_veh.selected_sit
execute rotated as @e[type=marker,tag=test.kill,limit=1] run tp @s ~ ~ ~ ~ ~
ride @s mount @e[type=llama,tag=phys_veh.selected_sit,limit=1]
tag @e[type=llama,tag=phys_veh.selected_sit,limit=1] remove phys_veh.selected_sit

kill @e[type=marker,tag=test.kill,limit=1]
