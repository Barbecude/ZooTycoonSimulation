execute store result storage midwut:main macros.pixel int 1 run random value 1..4
execute if predicate midwut:chance/0.1 store result storage midwut:main macros.pixel int 1 run random value 5..6
function physical_vehicles:setblock/road/material with storage midwut:main macros

#particle dust{color:[1f,0f,0f],scale:2f}

scoreboard players remove #z_ main_score 1
execute if score #z_ main_score matches 1.. positioned ~ ~ ~1 run function midwut:test/gen_roads/cobblestone/z