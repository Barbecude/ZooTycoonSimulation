execute unless block ~ ~ ~ soul_campfire[facing=north,lit=false,signal_fire=false,waterlogged=false] run setblock ~ ~ ~ minecraft:soul_campfire[facing=north,lit=false,signal_fire=false,waterlogged=false]
execute if data storage midwut:main {slot:-1} store result storage midwut:main data.Slot byte 1 store result score #slot main_score run function physical_vehicles:zzz/block/get_first_slot
execute unless data storage midwut:main {slot:-1} store result storage midwut:main data.Slot byte 1 store result score #slot main_score run data get storage midwut:main slot
execute if score #slot main_score matches -1 run return fail
data modify storage midwut:main side set from storage midwut:main args.strings[0]
execute if score #slot main_score matches 1.. run function physical_vehicles:zzz/block/guardrail/main
data modify storage midwut:main args.strings[0] set from storage midwut:main side
data modify storage midwut:main side set from storage midwut:main args.strings[1]
execute if score #slot main_score matches 1.. run function physical_vehicles:zzz/block/guardrail/main
data modify storage midwut:main args.strings[1] set from storage midwut:main side
data modify storage midwut:main side set from storage midwut:main args.strings[2]
execute if score #slot main_score matches 1.. run function physical_vehicles:zzz/block/guardrail/main
data modify storage midwut:main args.strings[2] set from storage midwut:main side


return 1