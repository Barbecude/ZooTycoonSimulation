
execute in midwut:main run data remove block 0 0 0 Items[{components:{"minecraft:custom_data":{midwut.custom_block:true}}}]
execute in midwut:main run loot give @s mine 0 0 0 minecraft:shears[minecraft:custom_data={drop:true}]
execute if data block ~ ~ ~ Items[{Slot:0b}] unless data block ~ ~ ~ Items[{Slot:0b,components:{"minecraft:custom_data":{midwut.custom_block:true}}}] run data remove block ~ ~ ~ Items[{Slot:0b}]
execute if data block ~ ~ ~ Items[{Slot:1b}] unless data block ~ ~ ~ Items[{Slot:1b,components:{"minecraft:custom_data":{midwut.custom_block:true}}}] run data remove block ~ ~ ~ Items[{Slot:1b}]
execute if data block ~ ~ ~ Items[{Slot:2b}] unless data block ~ ~ ~ Items[{Slot:2b,components:{"minecraft:custom_data":{midwut.custom_block:true}}}] run data remove block ~ ~ ~ Items[{Slot:2b}]
execute if data block ~ ~ ~ Items[{Slot:3b}] unless data block ~ ~ ~ Items[{Slot:3b,components:{"minecraft:custom_data":{midwut.custom_block:true}}}] run data remove block ~ ~ ~ Items[{Slot:3b}]

execute if block ~ ~ ~ minecraft:soul_campfire[lit=true] run return run function midwut:y_functions/custom_block_fixs/remove_lit

return 1