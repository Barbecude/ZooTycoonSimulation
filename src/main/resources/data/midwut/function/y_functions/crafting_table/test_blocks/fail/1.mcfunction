function midwut:y_functions/crafting_table/test_blocks/kill_soul_soil
execute positioned ~ ~1.5 ~ unless block ~ ~ ~ minecraft:iron_trapdoor[facing=south,open=true] as @e[type=minecraft:item,distance=..2,nbt={Age:0s,OnGround:0b}] if items entity @s contents minecraft:iron_trapdoor run kill

return fail
