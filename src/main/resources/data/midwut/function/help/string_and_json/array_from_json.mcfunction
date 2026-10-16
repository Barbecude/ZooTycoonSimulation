

# En
## Only on versions >= 1.19.4 !!!!!
# Ru
## Только на версиях >= 1.19.4 !!!!!


# En
## /data get storage midwut:main text.json — input data (in json format)
## /data get storage midwut:main text.array_of_characters — output data (array)

# Ru
## /data get storage midwut:main text.json — входные данные (в json формате)
## /data get storage midwut:main text.array_of_characters — выходные данные (массив)
 
data modify storage midwut:main text.array_of_characters set value []
execute unless data storage midwut:main text{json:''} run function midwut:y_functions/string_and_json/array_from_json


