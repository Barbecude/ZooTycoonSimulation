#data modify storage midwut:main Pos set from entity @s Pos
#execute store result storage midwut:main macros.px int 1 run data get storage midwut:main Pos[0]
#execute store result score #y main_score store result storage midwut:main macros.py int 0.01 run data get storage midwut:main Pos[1] 100
#execute store result storage midwut:main macros.pz int 1 run data get storage midwut:main Pos[2]
execute store result score #y main_score run data get entity @s Pos[1] 100

execute align xyz positioned ~0.5 ~0.5 ~0.5 run function physical_vehicles:zzz/tools/road_maker/plotting/block_storage with storage midwut:main macros
