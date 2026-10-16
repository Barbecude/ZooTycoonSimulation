$data modify storage midwut:main args set value $(args)

execute unless data storage midwut:main args.strings run return run tellraw @s [{"translate":"phys_veh.guardrail.strings_info","fallback":"Ожидался массив strings. Для отражателя — с двумя элементами, где оба это сторона света, но первый для балки, второй для стойки.\nБез отражателя — с тремя элементами, где все это стороны света, но первый для левой половины балки, второй для правой половины, третий для стойки."}]
execute unless data storage midwut:main args.flags run return run tellraw @s [{"translate":"phys_veh.guardrail.flags_info","fallback":"Ожидался массив flags с двумя элементами логического типа. Первый для отражателя, второй для стойки"}]
execute unless data storage midwut:main args.pixel run return run tellraw @s [{"translate":"phys_veh.guardrail.pixel_info","fallback":"Ожидался параметр pixel с высотой в пикселях"}]
execute unless data storage midwut:main args.slot run return run tellraw @s [{"translate":"phys_veh.guardrail.slot_info","fallback":"Ожидался параметр slot — -1 для автоматического выбора, от 0 до 3 для слота костра"}]

data modify storage midwut:main strings set from storage midwut:main args.strings
data modify storage midwut:main slot set from storage midwut:main args.slot
data modify storage midwut:main data set value {Slot:0b,components:{"minecraft:custom_data":{kill:true,midwut.custom_block:true,phys_veh.guardrail:true},"minecraft:custom_model_data":{strings:[],flags:[],floats:[0]}},count:1,id:"minecraft:jigsaw"}
execute unless function physical_vehicles:zzz/block/guardrail/block_g run return fail
execute if data storage midwut:main args{pixel:-1} run data modify storage midwut:main args.pixel set from block ~ ~ ~ Items[{components:{"minecraft:custom_data":{phys_veh.road:true}}}].components."minecraft:custom_model_data".floats[0]
execute if data storage midwut:main args{pixel:-1} run data modify storage midwut:main args.pixel set value 0
execute store result storage midwut:main data.components."minecraft:custom_data".height double 0.0625 run data get storage midwut:main args.pixel

data modify storage midwut:main data.components."minecraft:custom_model_data".strings set from storage midwut:main args.strings
data modify storage midwut:main data.components."minecraft:custom_model_data".flags set from storage midwut:main args.flags
data modify storage midwut:main data.components."minecraft:custom_model_data".floats[0] set from storage midwut:main args.pixel

data modify storage midwut:main side set from storage midwut:main strings[0]
function physical_vehicles:zzz/block/guardrail/3
data modify storage midwut:main data.components."minecraft:custom_data"."phys_veh.guardrail.right" set from storage midwut:main side
data modify storage midwut:main side set from storage midwut:main strings[1]
function physical_vehicles:zzz/block/guardrail/1
data modify storage midwut:main data.components."minecraft:custom_data"."phys_veh.guardrail.left" set from storage midwut:main side

data modify block ~ ~ ~ Items append from storage midwut:main data

execute store result score #pixel main_score run data get storage midwut:main args.pixel
execute if score #pixel main_score matches 3.. run function physical_vehicles:zzz/block/guardrail/upper