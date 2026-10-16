execute as @e[type=minecraft:item,distance=..2,nbt={Age:0s,OnGround:0b}] if items entity @s contents minecraft:soul_soil[!custom_data,!custom_name,!item_name,!lore,!custom_model_data] run kill
