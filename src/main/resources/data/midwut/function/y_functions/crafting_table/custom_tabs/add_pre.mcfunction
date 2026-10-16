#> midwut:y_functions/crafting_table/custom_tabs/add_pre
execute if data storage midwut:main midwut_crafting_table.tabs[0] run data modify storage midwut:main Tabs prepend value {id:"minecraft:structure_void",Slot:-1b,count:1,components:{"minecraft:custom_name":{"translate":"item.midwut.previous_tab","fallback":"Previous Tab","color":"white","italic":false},"minecraft:custom_data":{midwut.gui_item:true,kill:true,midwut.previous_tab:true},}}

execute store result score #test main_score run data get storage midwut:main Tabs
scoreboard players set #max main_score 27
scoreboard players set #page main_score 0
execute if score #test main_score matches 28.. run function midwut:y_functions/crafting_table/custom_tabs/pages
scoreboard players set #test2 main_score -1
execute store result storage midwut:main macros.temp int 1 run scoreboard players operation #page main_score *= #25 const
scoreboard players operation #temp main_score = #max main_score
function midwut:y_functions/crafting_table/custom_tabs/add with storage midwut:main macros
execute if score #max main_score matches 25 run function midwut:y_functions/crafting_table/custom_tabs/add_switches
