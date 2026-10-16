tp @s ~ ~ ~ ~ ~
data modify storage midwut:main Rotation set from entity @s Rotation
execute store result score #y main_score run data get storage midwut:main Rotation[0]
execute store result score #x main_score run data get storage midwut:main Rotation[1]
execute if score #x main_score matches ..-46 run setblock ~ ~ ~ piston[facing=up]
execute if score #x main_score matches 46.. run setblock ~ ~ ~ piston[facing=down]
execute if score #x main_score matches -45..45 run function midwut:test/setblock_facing/y
kill
