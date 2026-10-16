$data modify storage midwut:main side set value "$(side)"
$data modify storage midwut:main slot set value $(slot)
$data modify storage midwut:main pixel set value $(pixel)

execute if data storage midwut:main {side:"context"} as 0000051d-0000-0fb7-0000-001500000014 run function physical_vehicles:zzz/get_angle
execute unless data storage midwut:main {side:"north"} unless data storage midwut:main {side:"east"} unless data storage midwut:main {side:"south"} unless data storage midwut:main {side:"west"} run return fail

data modify storage midwut:main data set value {Slot:0b,components:{"minecraft:custom_data":{kill:true,midwut.custom_block:true,phys_veh.guardrail:true},"minecraft:custom_model_data":{strings:["north","","north"],flags:[true,false,false],floats:[0]}},count:1,id:"minecraft:jigsaw"}
execute unless function physical_vehicles:zzz/block/guardrail/block run return fail
execute if data storage midwut:main {pixel:-1} run data modify storage midwut:main pixel set from block ~ ~ ~ Items[{components:{"minecraft:custom_data":{phys_veh.road:true}}}].components."minecraft:custom_model_data".floats[0]
execute if data storage midwut:main {pixel:-1} run data modify storage midwut:main pixel set value 0
execute store result storage midwut:main data.components."minecraft:custom_data".height double 0.0625 run data get storage midwut:main pixel

data modify storage midwut:main data.components."minecraft:custom_model_data".strings[0] set from storage midwut:main side
data modify storage midwut:main data.components."minecraft:custom_model_data".strings[1] set from storage midwut:main side
data modify storage midwut:main data.components."minecraft:custom_model_data".strings[2] set from storage midwut:main side
$data modify storage midwut:main data.components."minecraft:custom_model_data".flags[1] set value bool($(post))
$data modify storage midwut:main data.components."minecraft:custom_model_data".flags[2] set value bool($(ironb))
data modify storage midwut:main data.components."minecraft:custom_model_data".floats[0] set from storage midwut:main pixel
execute if data storage midwut:main {side:"north"} run data modify storage midwut:main data.components."minecraft:custom_data" merge value {phys_veh.guardrail.right:"north",phys_veh.guardrail.left:"north"}
execute if data storage midwut:main {side:"east"} run data modify storage midwut:main data.components."minecraft:custom_data" merge value {phys_veh.guardrail.right:"east",phys_veh.guardrail.left:"east"}
execute if data storage midwut:main {side:"south"} run data modify storage midwut:main data.components."minecraft:custom_data" merge value {phys_veh.guardrail.right:"south",phys_veh.guardrail.left:"south"}
execute if data storage midwut:main {side:"west"} run data modify storage midwut:main data.components."minecraft:custom_data" merge value {phys_veh.guardrail.right:"west",phys_veh.guardrail.left:"west"}

data modify block ~ ~ ~ Items append from storage midwut:main data

execute store result score #pixel main_score run data get storage midwut:main pixel
execute if score #pixel main_score matches 3.. run function physical_vehicles:zzz/block/guardrail/upper

