playsound minecraft:ui.button.click master @s ~ ~ ~ 0.5 1
scoreboard players set #update_data main_score 1
data modify storage midwut:main midwut_crafting_table.tabs append from storage midwut:main midwut_crafting_table.tab
$data modify storage midwut:main midwut_crafting_table.tab set value "$(tag)"
