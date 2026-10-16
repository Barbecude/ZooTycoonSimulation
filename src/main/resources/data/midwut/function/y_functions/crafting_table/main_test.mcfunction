data modify storage midwut:main midwut_crafting_table set from storage midwut:main temp[-1]
$execute in $(dimension) positioned $(x) $(y) $(z) run function midwut:y_functions/crafting_table/main
data remove storage midwut:main temp[-1]
execute if data storage midwut:main temp[0] run function midwut:y_functions/crafting_table/main_test with storage midwut:main temp[-1]


