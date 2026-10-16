#> midwut:y_functions/crafting_table/switches
execute if score #update_data main_score matches 0 if function midwut:y_functions/crafting_table/gui_item_in_player run scoreboard players set #update_data main_score 1
execute if function midwut:y_functions/crafting_table/buttons/page_previous_clear run function midwut:y_functions/crafting_table/buttons/page_previous
execute if function midwut:y_functions/crafting_table/buttons/page_next_clear run function midwut:y_functions/crafting_table/buttons/page_next
execute if function midwut:y_functions/crafting_table/buttons/previous_tab_clear run function midwut:y_functions/crafting_table/buttons/previous_tab
function #midwut:crafting_table/action
clear @s *[minecraft:custom_data~{midwut.gui_item:true}]
