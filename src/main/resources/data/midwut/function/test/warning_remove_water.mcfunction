execute positioned ~1 ~ ~ if block ~ ~ ~ water run setblock ~ ~ ~ minecraft:command_block{Command:"function midwut:test/warning_remove_water",auto:1b}
execute positioned ~-1 ~ ~ if block ~ ~ ~ water run setblock ~ ~ ~ minecraft:command_block{Command:"function midwut:test/warning_remove_water",auto:1b}
execute positioned ~ ~ ~1 if block ~ ~ ~ water run setblock ~ ~ ~ minecraft:command_block{Command:"function midwut:test/warning_remove_water",auto:1b}
execute positioned ~ ~ ~-1 if block ~ ~ ~ water run setblock ~ ~ ~ minecraft:command_block{Command:"function midwut:test/warning_remove_water",auto:1b}
execute positioned ~ ~-1 ~ if block ~ ~ ~ water run setblock ~ ~ ~ minecraft:command_block{Command:"function midwut:test/warning_remove_water",auto:1b}
execute positioned ~ ~1 ~ if block ~ ~ ~ water run setblock ~ ~ ~ minecraft:command_block{Command:"function midwut:test/warning_remove_water",auto:1b}

setblock ~ ~ ~ minecraft:sponge
setblock ~ ~ ~ air
#playsound item.bucket.fill block @a ~ ~ ~