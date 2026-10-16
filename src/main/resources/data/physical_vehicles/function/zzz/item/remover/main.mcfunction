summon minecraft:item ~ ~ ~ {Item:{id:"minecraft:dolphin_spawn_egg",count:1,components:{"minecraft:entity_data":{Tags:["phys_veh.spawn","phys_veh.remover"],id:"minecraft:pig",Silent:true,NoAI:true,Invulnerable:true,DeathLootTable:""},"minecraft:custom_model_data":20019,"minecraft:custom_name":{translate:"phys_veh.remover",fallback:"Vehicles Remover"},"minecraft:rarity":"uncommon","minecraft:lore":[{translate:"phys_veh.remover.lore",italic:false,color:"gray"},{translate:"phys_veh.remover.lore2",italic:false,color:"gray"}]}},Tags:["get_id"]}
execute if data entity @s CustomName run data modify entity @e[type=minecraft:item,tag=get_id,limit=1,distance=..0.1] Item.components."minecraft:custom_data" set from entity @s CustomName
tag @e[type=minecraft:item,tag=get_id,limit=1,distance=..0.1] remove get_id
tp @s ~ -1000 ~
kill @s

execute as @n[type=minecraft:text_display,tag=phys_veh.main,distance=..10] run function physical_vehicles:zzz/item/remover/veh_item


