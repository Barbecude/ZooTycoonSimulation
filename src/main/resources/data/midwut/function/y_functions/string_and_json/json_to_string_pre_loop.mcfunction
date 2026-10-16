scoreboard players operation #characters main_score = #raycast main_score
execute store result score #test main_score if data storage midwut:main {temp2:"\\"}
function midwut:y_functions/string_and_json/json_to_string_loop

data modify storage midwut:main text.array_of_characters set from storage midwut:main temp
execute store result storage midwut:main trigonometric.in int 1 run scoreboard players add #characters main_score 9
function midwut:y_functions/string_and_json/json_to_string_macro with storage midwut:main trigonometric
