tag @s add midwut.crafting_table
data modify entity @s CustomName set from storage midwut:main temp[-1].CustomName
execute store result score #temp main_score run data get storage midwut:main temp[-1].mirrored
execute if score #temp main_score matches 1 run tag @s add midwut.crafting_table.mirrored
execute store result score @s midwut.crafting_table.id run data get storage midwut:main temp[-1].id
execute store result score #temp main_score run data get storage midwut:main temp[-1].facing
execute if score #temp main_score matches 0 run tag @s add midwut.crafting_table.south
execute if score #temp main_score matches 1 run tag @s add midwut.crafting_table.west
execute if score #temp main_score matches 2 run tag @s add midwut.crafting_table.north
execute if score #temp main_score matches 3 run tag @s add midwut.crafting_table.east

