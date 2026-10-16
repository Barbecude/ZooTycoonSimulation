
# En
## Only on versions >= 1.16 !!!!!
# Ru
## Только на версиях >= 1.16 !!!!!


# En
## /data get storage midwut:main text.string — input data (in string format)
## /data get storage midwut:main text.array — output data (array of strings)

# Ru
## /data get storage midwut:main text.string — входные данные (в string формате)
## /data get storage midwut:main text.array — выходные данные (массив sting'ов)

data modify storage midwut:main text.array set value []
execute store result score #test main_score run data get storage midwut:main text.string
execute if score #test main_score matches 1.. run function midwut:y_functions/string_and_json/string_split/pre

#execute if score #mc_version main_score matches 16.. in midwut:main positioned 0 1 0 run function midwut:y_functions/string_to_json
#execute if score #mc_version main_score matches ..15 in minecraft:overworld positioned 1 0 1 run function midwut:y_functions/string_to_json