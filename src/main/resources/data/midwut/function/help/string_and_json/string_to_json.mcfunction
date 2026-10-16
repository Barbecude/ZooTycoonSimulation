
# En
## Only on versions >= 1.16 !!!!!
# Ru
## Только на версиях >= 1.16 !!!!!


# En
## /data get storage midwut:main text.string — input data (in string format)
## /data get storage midwut:main text.json — output data (in json text format)

# Ru
## /data get storage midwut:main text.string — входные данные (в string формате)
## /data get storage midwut:main text.json — выходные данные (в json text формате)

execute in midwut:main run function midwut:y_functions/string_and_json/string_to_json
#execute if score #mc_version main_score matches 16.. in midwut:main positioned 0 1 0 run function midwut:y_functions/string_to_json
#execute if score #mc_version main_score matches ..15 in minecraft:overworld positioned 1 0 1 run function midwut:y_functions/string_to_json