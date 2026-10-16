#particle dust 1 0 0 1 ~ ~ ~ 0 0 0 0 1 force

execute if block ~ ~ ~ minecraft:soul_campfire{Items:[{components:{"minecraft:custom_data":{midwut.custom_block:true}}}]} if function midwut:y_functions/custom_block_fixs/raycast_check run return 1
execute positioned ~0.5 ~ ~0.5 if block ~ ~ ~ minecraft:soul_campfire{Items:[{components:{"minecraft:custom_data":{midwut.custom_block:true}}}]} if function midwut:y_functions/custom_block_fixs/raycast_check run return 1
execute positioned ~0.5 ~ ~-0.5 if block ~ ~ ~ minecraft:soul_campfire{Items:[{components:{"minecraft:custom_data":{midwut.custom_block:true}}}]} if function midwut:y_functions/custom_block_fixs/raycast_check run return 1
execute positioned ~-0.5 ~ ~0.5 if block ~ ~ ~ minecraft:soul_campfire{Items:[{components:{"minecraft:custom_data":{midwut.custom_block:true}}}]} if function midwut:y_functions/custom_block_fixs/raycast_check run return 1
execute positioned ~-0.5 ~ ~-0.5 if block ~ ~ ~ minecraft:soul_campfire{Items:[{components:{"minecraft:custom_data":{midwut.custom_block:true}}}]} if function midwut:y_functions/custom_block_fixs/raycast_check run return 1

scoreboard players remove #distance main_score 1
execute if score #distance main_score matches 1.. positioned ^ ^ ^0.1 run function midwut:y_functions/custom_block_fixs/raycast