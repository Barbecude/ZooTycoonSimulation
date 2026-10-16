
data modify storage midwut:main temp2 set from storage midwut:main text.string
data modify storage midwut:main temp3 set from storage midwut:main text.string
execute store result score #test3 main_score run scoreboard players set #test2 main_score 0
function midwut:y_functions/string_and_json/string_split/loop
execute unless data storage midwut:main {temp3:""} run data modify storage midwut:main text.array append from storage midwut:main temp3
