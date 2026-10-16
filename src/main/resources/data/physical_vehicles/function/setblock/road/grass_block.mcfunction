setblock ~ ~ ~ air

$execute if score #16 const matches ..$(pixel) run return run setblock ~ ~ ~ grass_block
$setblock ~ ~ ~ minecraft:soul_campfire[facing=north,lit=false,signal_fire=false,waterlogged=false]{Items:[{Slot:0b,components:{"minecraft:custom_data":{height:0.0d,kill:true,midwut.custom_block:true,phys_veh.road:true},"minecraft:custom_model_data":{strings:["grass_block"],floats:[$(pixel)]}},count:1,id:"minecraft:jigsaw"}]}

$execute store result block ~ ~ ~ Items[0].components."minecraft:custom_data".height double 0.0625 run scoreboard players set #temp main_score $(pixel)