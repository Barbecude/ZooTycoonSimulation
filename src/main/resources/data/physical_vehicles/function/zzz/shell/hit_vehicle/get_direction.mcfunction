data modify storage midwut:main Pos set from entity @s Pos
execute store result score #impx main_score run data get storage midwut:main Pos[0] 1000
execute store result score #impy main_score run data get storage midwut:main Pos[1] 1000
execute store result score #impz main_score run data get storage midwut:main Pos[2] 1000