scoreboard players set #update_data main_score 1
data modify entity @s item.tag.tabs append from entity @s item.tag.tab
function midwut:y_functions/crafting_table/changing_tab_macro_tag with entity @s item.tag
$data modify entity @s item.tag.tab set value $(tag)
$tag @s add $(tag)
