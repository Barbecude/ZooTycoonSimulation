setblock ~ ~1 ~ minecraft:soul_campfire[facing=north,lit=false,signal_fire=false,waterlogged=false]
execute store result storage midwut:main data.components."minecraft:custom_data".height double 0.0625 run scoreboard players remove #pixel main_score 16
data modify storage midwut:main data.components."minecraft:custom_model_data".floats set value [-1]
data modify block ~ ~1 ~ Items append from storage midwut:main data