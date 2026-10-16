#execute store result score #yh main_score run data get storage midwut:main road_maker_data[-1].yh
#execute store result score #yc main_score run data get storage midwut:main road_maker_data[-1].yc

scoreboard players operation #y main_score = @s phys_veh.offset.y
scoreboard players operation #c main_score = @s phys_veh.count_wheels
execute positioned ~ ~-1 ~ if function physical_vehicles:zzz/tools/road_maker/make/combine run return run kill

scoreboard players operation #y main_score /= #c main_score
#scoreboard players operation #y main_score %= #100 const
execute store result score #h main_score run data get entity @s Pos[1] 100
scoreboard players remove #h main_score 50
scoreboard players operation #y main_score -= #h main_score
#function physical_vehicles:zzz/tools/road_maker/make/pos with storage midwut:main road_maker_data[-1]
function physical_vehicles:zzz/tools/road_maker/make/ray

#data remove storage midwut:main road_maker_data[-1]
#execute if data storage midwut:main road_maker_data[0] run function physical_vehicles:zzz/tools/road_maker/make/loop