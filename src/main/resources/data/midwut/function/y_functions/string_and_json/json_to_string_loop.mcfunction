scoreboard players remove #raycast main_score 1
data modify storage midwut:main temp2 set string storage midwut:main temp3 0 1
execute unless score #test main_score matches 1.. if data storage midwut:main {temp2:'"'} run function midwut:y_functions/string_and_json/json_to_string_stop
execute store result score #test main_score if data storage midwut:main {temp2:"\\"}
data modify storage midwut:main temp3 set string storage midwut:main temp3 1

execute if score #raycast main_score matches 1.. run function midwut:y_functions/string_and_json/json_to_string_loop


