loot spawn ~ ~ ~ loot {"pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:structure_void","functions":[{"function":"minecraft:set_custom_data","tag":"{midwut.spawned_item:true}"}]}]}]}
execute as @e[type=minecraft:item,distance=..0.1] if items entity @s contents *[minecraft:custom_data~{midwut.spawned_item:true}] run data modify entity @s Item set from storage midwut:main Data.Item


