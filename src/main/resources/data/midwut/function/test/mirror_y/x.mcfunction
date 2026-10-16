scoreboard players remove #tx main_score 1

$clone ~ ~ ~ ~ ~ ~ ~ ~$(y) ~

$execute if block ~ ~$(y) ~ #midwut:all/1.21.11[half=bottom,facing=north] run setblock ~ ~$(y) ~ deepslate_tile_stairs[half=top,facing=north]
$execute if block ~ ~$(y) ~ #midwut:all/1.21.11[half=bottom,facing=south] run setblock ~ ~$(y) ~ deepslate_tile_stairs[half=top,facing=south]
$execute if block ~ ~$(y) ~ #midwut:all/1.21.11[type=bottom] run setblock ~ ~$(y) ~ deepslate_tile_slab[type=top]

execute if score #tx main_score matches 1.. positioned ~1 ~ ~ run function midwut:test/mirror_y/x with storage midwut:main macros