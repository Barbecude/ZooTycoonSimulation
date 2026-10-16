data modify storage midwut:main temp2 set string storage midwut:main text.json 9 10
data modify storage midwut:main temp3 set string storage midwut:main text.json 10
scoreboard players set #test main_score 0
execute store result score #raycast main_score run data get storage midwut:main temp3
execute unless score #raycast main_score matches 3.. run data modify storage midwut:main text.array_of_characters append from storage midwut:main temp2
execute if score #raycast main_score matches 3.. run function midwut:y_functions/string_and_json/array_from_json_loop
