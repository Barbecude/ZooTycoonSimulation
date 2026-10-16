execute positioned ~1 ~ ~ if block ~ ~ ~ #midwut:run unless block ~ ~ ~ water[level=0] run setblock ~ ~ ~ minecraft:command_block{Command:"function midwut:test/warning_fill",auto:1b}
execute positioned ~-1 ~ ~ if block ~ ~ ~ #midwut:run unless block ~ ~ ~ water[level=0] run setblock ~ ~ ~ minecraft:command_block{Command:"function midwut:test/warning_fill",auto:1b}
execute positioned ~ ~ ~1 if block ~ ~ ~ #midwut:run unless block ~ ~ ~ water[level=0] run setblock ~ ~ ~ minecraft:command_block{Command:"function midwut:test/warning_fill",auto:1b}
execute positioned ~ ~ ~-1 if block ~ ~ ~ #midwut:run unless block ~ ~ ~ water[level=0] run setblock ~ ~ ~ minecraft:command_block{Command:"function midwut:test/warning_fill",auto:1b}
execute positioned ~ ~-1 ~ if block ~ ~ ~ #midwut:run unless block ~ ~ ~ water[level=0] run setblock ~ ~ ~ minecraft:command_block{Command:"function midwut:test/warning_fill",auto:1b}

setblock ~ ~ ~ water[level=0]
playsound item.bucket.empty block @a ~ ~ ~