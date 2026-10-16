
tag @s remove midwut.crafting_table.spawn
tag @s add midwut.crafting_table
setblock ~ ~ ~ minecraft:barrel{CustomName:{"translate": "item.midwut.crafting_table","fallback": "MIDWUT Crafting Station"}}
data modify block ~ ~ ~ CustomName set from entity @s CustomName

execute if score #temp main_score matches 4.. run tag @s add midwut.crafting_table.mirrored
execute if score #temp main_score matches 0 run function midwut:y_functions/crafting_table/install_end/0
execute if score #temp main_score matches 1 run function midwut:y_functions/crafting_table/install_end/1
execute if score #temp main_score matches 2 run function midwut:y_functions/crafting_table/install_end/2
execute if score #temp main_score matches 3 run function midwut:y_functions/crafting_table/install_end/3
execute if score #temp main_score matches 4 run function midwut:y_functions/crafting_table/install_end/4
execute if score #temp main_score matches 5 run function midwut:y_functions/crafting_table/install_end/5
execute if score #temp main_score matches 6 run function midwut:y_functions/crafting_table/install_end/6
execute if score #temp main_score matches 7 run function midwut:y_functions/crafting_table/install_end/7


execute unless data storage midwut:main midwut_crafting_tables[0] run schedule function midwut:tick_crafting_table 1t replace
execute unless data storage midwut:main midwut_crafting_tables[0] run schedule function midwut:y_functions/crafting_table/cancel_kill/tick 1s replace
data modify storage midwut:main midwut_crafting_tables append from block ~ ~ ~
data modify storage midwut:main midwut_crafting_tables[-1].dimension set from entity @a[tag=midwut.used.dolphin_spawn_egg.selected_player,limit=1] Dimension
data modify storage midwut:main midwut_crafting_tables[-1].CustomName set from entity @s CustomName
data modify storage midwut:main midwut_crafting_tables[-1] merge value {tabs:[],tab:"midwut.crafting_table.main_tab",page:0,previous_page:0}
execute store result storage midwut:main midwut_crafting_tables[-1].mirrored byte 1 if entity @s[tag=midwut.crafting_table.mirrored]
execute store result storage midwut:main midwut_crafting_tables[-1].facing byte 1 run scoreboard players get #temp main_score
execute store result storage midwut:main midwut_crafting_tables[-1].id int 1 run scoreboard players add #midwut.crafting_table_id main_score 1
scoreboard players operation @s midwut.crafting_table.id = #midwut.crafting_table_id main_score
execute if score #temp main_score matches 0 run tag @s add midwut.crafting_table.south
execute if score #temp main_score matches 1 run tag @s add midwut.crafting_table.west
execute if score #temp main_score matches 2 run tag @s add midwut.crafting_table.north
execute if score #temp main_score matches 3 run tag @s add midwut.crafting_table.east
