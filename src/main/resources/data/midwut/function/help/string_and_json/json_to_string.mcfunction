
# En
## Only on versions >= 1.15 !!!!!
# Ru
## Только на версиях >= 1.15 !!!!!
## function midwut:help/string_and_json/json_to_string

# En
## /data get storage midwut:main text.json — input data (in json format)
## /data get storage midwut:main text.string — output data (in string text format)

# Ru
## /data get storage midwut:main text.json — входные данные (в json формате)
## /data get storage midwut:main text.string — выходные данные (в string text формате)

data modify storage midwut:main temp2 set string storage midwut:main text.json 9 11
execute if data storage midwut:main {temp2:'"}'} unless data storage midwut:main text{string:''} run data modify storage midwut:main text.string set value ''
execute unless data storage midwut:main {temp2:'"}'} run function midwut:y_functions/string_and_json/json_to_string
#data modify storage midwut:main text.string set string storage midwut:main text.json
#execute if score #mc_version main_score matches 16.. in midwut:main positioned 0 1 0 run function midwut:y_functions/string_to_json
#execute if score #mc_version main_score matches ..15 in minecraft:overworld positioned 1 0 1 run function midwut:y_functions/string_to_json


