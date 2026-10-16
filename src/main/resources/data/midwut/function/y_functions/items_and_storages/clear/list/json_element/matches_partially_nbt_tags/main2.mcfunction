data remove storage midwut:main Items2[-1]
data modify storage midwut:main temp set from storage midwut:main Inventory[-1]
data modify storage midwut:main macros.temp set from storage midwut:main Items[-1]
function midwut:y_functions/items_and_storages/clear/list/json_element/matches_partially_nbt_tags/test with storage midwut:main macros
execute if score #test main_score matches 1 if data storage midwut:main Items2[1] run function midwut:y_functions/items_and_storages/clear/list/json_element/matches_partially_nbt_tags/main2