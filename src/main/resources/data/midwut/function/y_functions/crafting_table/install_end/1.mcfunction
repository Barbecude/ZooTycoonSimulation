execute store result score #midwut.doTileDrops main_score run gamerule block_drops
gamerule block_drops false
execute if block ~ ~ ~1 minecraft:chest[waterlogged=false] run setblock ~ ~ ~1 minecraft:soul_campfire[facing=north,lit=false,signal_fire=false,waterlogged=false]{Items:[{Slot:1b,id:"minecraft:glow_berries",count:1,components:{"minecraft:custom_data":{midwut.custom_block:true,midwut.custom_block.vanilla_hitbox:true,kill:true},}}]} destroy
execute if block ~ ~ ~1 minecraft:chest[waterlogged=true] run setblock ~ ~ ~1 minecraft:soul_campfire[facing=north,lit=false,signal_fire=false,waterlogged=true]{Items:[{Slot:1b,id:"minecraft:glow_berries",count:1,components:{"minecraft:custom_data":{midwut.custom_block:true,midwut.custom_block.vanilla_hitbox:true,kill:true},}}]} destroy
execute if score #midwut.doTileDrops main_score matches 1 run gamerule block_drops true
execute if block ~ ~1 ~ #midwut:air_and_water run setblock ~ ~1 ~ minecraft:iron_trapdoor[facing=south,open=true,waterlogged=false]
execute if block ~ ~1 ~ minecraft:water run setblock ~ ~1 ~ minecraft:iron_trapdoor[facing=south,open=true,waterlogged=true]
execute positioned ~-1 ~ ~1 if block ~ ~ ~ minecraft:iron_trapdoor[waterlogged=false] run setblock ~ ~ ~ minecraft:air
execute positioned ~-2 ~ ~1 if block ~ ~ ~ minecraft:iron_trapdoor[waterlogged=false] run setblock ~ ~ ~ minecraft:air
execute positioned ~-1 ~ ~ if block ~ ~ ~ minecraft:iron_trapdoor[waterlogged=false] run setblock ~ ~ ~ minecraft:air
execute positioned ~-2 ~ ~ if block ~ ~ ~ minecraft:iron_trapdoor[waterlogged=false] run setblock ~ ~ ~ minecraft:air
execute positioned ~-1 ~ ~1 if block ~ ~ ~ minecraft:iron_trapdoor[waterlogged=true] run setblock ~ ~ ~ minecraft:water
execute positioned ~-2 ~ ~1 if block ~ ~ ~ minecraft:iron_trapdoor[waterlogged=true] run setblock ~ ~ ~ minecraft:water
execute positioned ~-1 ~ ~ if block ~ ~ ~ minecraft:iron_trapdoor[waterlogged=true] run setblock ~ ~ ~ minecraft:water
execute positioned ~-2 ~ ~ if block ~ ~ ~ minecraft:iron_trapdoor[waterlogged=true] run setblock ~ ~ ~ minecraft:water
