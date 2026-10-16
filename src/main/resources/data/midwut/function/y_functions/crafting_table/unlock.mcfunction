data remove block ~ ~ ~ lock
data modify storage midwut:main midwut_crafting_table.tab set from storage midwut:main midwut_crafting_table.tabs[0]
data modify storage midwut:main midwut_crafting_table.tabs set value []
data modify storage midwut:main midwut_crafting_table.page set value 0
scoreboard players set #update_data main_score 1
