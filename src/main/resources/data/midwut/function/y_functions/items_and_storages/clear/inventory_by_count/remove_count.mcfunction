execute store result score #temp main_score run data get storage midwut:main Inventory[-1].Count
execute store result score #temp2 main_score store result score #temp3 main_score run data get storage midwut:main Items2[-1].Count
scoreboard players operation #temp2 main_score < #temp main_score
scoreboard players operation #midwut.out main_score += #temp2 main_score
execute store result storage midwut:main Inventory[-1].Count byte 1 run scoreboard players operation #temp main_score -= #temp2 main_score
execute store result storage midwut:main Items2[-1].Count byte 1 run scoreboard players operation #temp3 main_score -= #temp2 main_score
execute if score #temp3 main_score matches 0 run data remove storage midwut:main Items2[-1]
execute unless score #temp main_score matches 0 run data modify storage midwut:main Inventory2 prepend from storage midwut:main Inventory[-1]

execute if data storage midwut:main Items2[0] run return fail
execute if data storage midwut:main Items3[0] run return fail

data remove storage midwut:main Inventory[-1]
data modify storage midwut:main Inventory2 prepend from storage midwut:main Inventory[]
data modify storage midwut:main Inventory set value []

