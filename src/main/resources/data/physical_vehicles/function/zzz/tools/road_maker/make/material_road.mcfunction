#data modify storage midwut:main road_exclude set value [{b:"light_gray_concrete"},{b:"#slabs[type=double]"},{b:"#slabs[type=top]"}]
data modify storage midwut:main road_exclude_ set from storage midwut:main road_exclude
execute if function physical_vehicles:zzz/tools/road_maker/make/material_exclude_loop_pre run return fail

execute if data block ~ ~ ~ Items[{components:{"minecraft:custom_data":{phys_veh.road:true}}}] run return run data modify storage midwut:main output_block.block.Name set from block ~ ~ ~ Items[{components:{"minecraft:custom_data":{phys_veh.road:true}}}].components."minecraft:custom_model_data".strings[0]

execute unless block ~ ~ ~ #midwut:pixels/all run return run function midwut:help/get_block/blockstate
execute positioned ~ ~-1 ~ unless block ~ ~ ~ #midwut:pixels/all run return run function midwut:help/get_block/blockstate