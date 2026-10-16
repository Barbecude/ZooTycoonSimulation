scoreboard players set #max main_score 25
scoreboard players operation #test2 main_score = #test main_score
scoreboard players operation #test2 main_score /= #25 const
scoreboard players add #test2 main_score 1
execute store result score #page main_score run data get storage midwut:main midwut_crafting_table.page
execute store result storage midwut:main midwut_crafting_table.page int 1 run scoreboard players operation #page main_score %= #test2 main_score
scoreboard players set #test main_score 25
