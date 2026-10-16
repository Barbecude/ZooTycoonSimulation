
#data modify storage midwut:main temp3 set string storage midwut:main temp2 0 1
#data modify storage midwut:main text.array_of_characters append from storage midwut:main temp3
data modify storage midwut:main text.array_of_characters append string storage midwut:main temp2 0 1
data modify storage midwut:main temp2 set string storage midwut:main temp2 1

scoreboard players remove #raycast main_score 1
execute if score #raycast main_score matches 1.. run function midwut:y_functions/string_and_json/array_from_string_loop


