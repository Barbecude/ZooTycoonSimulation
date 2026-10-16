#> midwut:y_functions/crafting_table/run_tabs
data modify storage midwut:main Tabs set value []
data modify block ~ ~ ~ Items set value []

function midwut:y_functions/crafting_table/layer_sel

function midwut:y_functions/crafting_table/custom_tabs/add_pre

$data modify storage midwut:main midwut_crafting_tables[{id:$(id)}] set from storage midwut:main midwut_crafting_table