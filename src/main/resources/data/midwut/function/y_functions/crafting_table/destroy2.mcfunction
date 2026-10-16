particle minecraft:dust{color:[0.439f,0.439f,0.439f],scale:2f} ~ ~1 ~ 0.2 0.2 0.2 1 5 normal
loot spawn ~ ~ ~ loot midwut:crafting_table/main
#data modify entity @e[type=minecraft:item,limit=1,distance=..0.1,nbt={Item:{tag:{EntityTag: {id: "minecraft:item_display", Tags: ["midwut.crafting_table", "midwut.crafting_table.spawn"]}}}}] Item.tag.display.Name set from entity @s CustomName
kill @s[type=!player]



