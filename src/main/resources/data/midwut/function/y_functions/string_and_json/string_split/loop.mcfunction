data modify storage midwut:main temp set string storage midwut:main temp2 0 1
data modify storage midwut:main temp2 set string storage midwut:main temp2 1
execute if data storage midwut:main {temp:" "} unless score #test3 main_score matches 0 run function midwut:y_functions/string_and_json/string_split/space

scoreboard players add #test2 main_score 1
scoreboard players add #test3 main_score 1
execute if score #test2 main_score <= #test main_score run function midwut:y_functions/string_and_json/string_split/loop