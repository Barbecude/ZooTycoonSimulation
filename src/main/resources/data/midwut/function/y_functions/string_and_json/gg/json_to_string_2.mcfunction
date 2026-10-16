scoreboard players remove #raycast main_score 1

data modify storage midwut:main temp append from storage midwut:main temp2
data modify storage midwut:main temp2 set string storage midwut:main temp3 0 1
execute if score #test main_score matches 0 if data storage midwut:main {temp2:'"'} run function midwut:y_functions/string_and_json/json_to_string_3
scoreboard players set #test main_score 0
execute store success score #test main_score if data storage midwut:main {temp2:'\\'} run scoreboard players set #test main_score 1
data modify storage midwut:main temp3 set string storage midwut:main temp3 1

execute if score #raycast main_score matches 1.. run function midwut:y_functions/string_and_json/json_to_string_2


