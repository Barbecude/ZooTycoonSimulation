#> midwut:y_functions/crafting_table/destroy/main
loot spawn ~ ~ ~ loot midwut:crafting_table/main
#data modify entity @e[type=minecraft:item,limit=1,distance=..0.1,nbt={Age:0s,OnGround:0b,Item:{tag:{EntityTag:{Tags:["midwut.crafting_table.spawn"],id:"minecraft:marker"}}}}] Item.tag.display.Name set value '$(CustomName)'
loot spawn ~ ~ ~ loot midwut:crafting_table/items

execute if entity @s[tag=midwut.crafting_table.south] run function midwut:y_functions/crafting_table/destroy/south
execute if entity @s[tag=midwut.crafting_table.west] run function midwut:y_functions/crafting_table/destroy/west
execute if entity @s[tag=midwut.crafting_table.north] run function midwut:y_functions/crafting_table/destroy/north
execute if entity @s[tag=midwut.crafting_table.east] run function midwut:y_functions/crafting_table/destroy/east

execute positioned ~ ~1 ~ run function midwut:y_functions/crafting_table/destroy/check_trapdoor
execute if block ~ ~ ~ minecraft:barrel if items block ~ ~ ~ container.* *[!minecraft:custom_data~{midwut.gui_item:true}] run setblock ~ ~ ~ air destroy
execute if block ~ ~ ~ minecraft:barrel run setblock ~ ~ ~ air
execute as @e[type=minecraft:item,distance=..8] if items entity @s contents *[minecraft:custom_data~{kill:true}] run kill

execute as @e[type=minecraft:item,distance=..1,nbt={Age:0s,OnGround:0b}] if items entity @s contents minecraft:barrel[!custom_data,!custom_name,!item_name,!lore,!custom_model_data] run kill
$data remove storage midwut:main midwut_crafting_tables[{id:$(id)}]
kill @s[type=!minecraft:player]