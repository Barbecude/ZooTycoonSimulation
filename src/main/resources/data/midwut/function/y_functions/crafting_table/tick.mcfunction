#> midwut:y_functions/crafting_table/tick

execute if items block ~ ~-1 ~ container.* *[minecraft:custom_data~{kill:true}] store result score #update_data main_score run data remove block ~ ~-1 ~ Items[{components:{"minecraft:custom_data":{kill:true}}}]
execute as @e[type=minecraft:hopper_minecart,distance=..5] if items entity @s container.* *[minecraft:custom_data~{kill:true}] store result score #update_data main_score run data remove entity @s Items[{components:{"minecraft:custom_data":{kill:true}}}]

execute if block ~ ~ ~ minecraft:barrel[open=false] unless data block ~ ~ ~ lock run return fail
$data modify storage midwut:main midwut_crafting_table set from storage midwut:main midwut_crafting_tables[{id:$(id)}]

tag @s add midwut.selected_crafting_table
execute if block ~ ~ ~ minecraft:barrel[open=false] if data block ~ ~ ~ lock run function midwut:y_functions/crafting_table/unlock
execute if block ~ ~ ~ minecraft:barrel[open=true] unless data block ~ ~ ~ lock run function midwut:y_functions/crafting_table/lock

execute if items block ~ ~ ~ container.* *[!minecraft:custom_data~{midwut.gui_item:true}] run function midwut:y_functions/crafting_table/air_slots
execute as @a[distance=..10] run function midwut:y_functions/crafting_table/switches

#execute if function midwut:y_functions/crafting_table/previous_page_check if score #update_data main_score matches 0 run scoreboard players set #update_data main_score 1

execute if score #update_data main_score matches 1.. run function midwut:y_functions/crafting_table/run_tabs with storage midwut:main macros

tag @s remove midwut.selected_crafting_table