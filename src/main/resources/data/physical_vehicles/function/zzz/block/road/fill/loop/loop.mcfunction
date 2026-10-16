$setblock ~ ~ ~ $(temp)
execute if score #up main_score matches 1 positioned ~ ~1 ~ if block ~ ~ ~ #midwut:run run function physical_vehicles:zzz/block/road/fill/loop/loop with storage midwut:main macros
execute if score #down main_score matches 1 positioned ~ ~-1 ~ if block ~ ~ ~ #midwut:run run function physical_vehicles:zzz/block/road/fill/loop/loop with storage midwut:main macros
execute if score #east main_score matches 1 positioned ~1 ~ ~ if block ~ ~ ~ #midwut:run run function physical_vehicles:zzz/block/road/fill/loop/loop with storage midwut:main macros
execute if score #west main_score matches 1 positioned ~-1 ~ ~ if block ~ ~ ~ #midwut:run run function physical_vehicles:zzz/block/road/fill/loop/loop with storage midwut:main macros
execute if score #south main_score matches 1 positioned ~ ~ ~1 if block ~ ~ ~ #midwut:run run function physical_vehicles:zzz/block/road/fill/loop/loop with storage midwut:main macros
execute if score #north main_score matches 1 positioned ~ ~ ~-1 if block ~ ~ ~ #midwut:run run function physical_vehicles:zzz/block/road/fill/loop/loop with storage midwut:main macros