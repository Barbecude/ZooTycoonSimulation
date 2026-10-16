scoreboard players set #raycast main_score 8
data remove storage midwut:main output_block
data remove storage midwut:main macros.material
function physical_vehicles:zzz/tools/road_maker/make/material_loop

execute unless data storage midwut:main output_block run return fail

function physical_vehicles:zzz/tools/road_maker/make/cut_main
data modify storage midwut:main macros.colors set value []
execute store result storage midwut:main macros.pixel int 0.16 run scoreboard players add #y main_score 3
execute if data block ~ ~1 ~ Items[{components:{"minecraft:custom_data":{phys_veh.road:true}}}] run data modify storage midwut:main macros.pixel set value 16
function physical_vehicles:setblock/road/material with storage midwut:main macros
execute if score #y main_score matches 7.. if data block ~ ~-1 ~ Items[{components:{"minecraft:custom_data":{phys_veh.road:true}}}] positioned ~ ~-1 ~ run return run function physical_vehicles:zzz/tools/road_maker/make/road_below
execute if score #y main_score matches 7.. run function physical_vehicles:zzz/tools/road_maker/make/set_below with storage midwut:main macros
