data modify storage midwut:main temp set from block ~ ~ ~ Items
execute store result score #temp main_score if block ~ ~ ~ minecraft:soul_campfire[waterlogged=true]
execute if score #temp main_score matches 0 run setblock ~ ~ ~ minecraft:soul_campfire[facing=north,lit=false,signal_fire=false,waterlogged=false]
execute if score #temp main_score matches 1 run setblock ~ ~ ~ minecraft:soul_campfire[facing=north,lit=false,signal_fire=false,waterlogged=true]
data modify block ~ ~ ~ Items set from storage midwut:main temp
return 1