execute store result score #temp main_score run data get storage midwut:main gears[0] 10

execute if score #temp main_score matches 0.. if score #temp2 main_score matches ..-1 run scoreboard players operation @s phys_veh.gear_num_non_neg_index = #gear_counter main_score
scoreboard players add #gear_counter main_score 1

execute if score #temp main_score matches 1.. if score #temp2 main_score matches ..-1 run tag @s add phys_veh.doesnt_have_neutral_gear
execute if score #temp main_score matches 1.. run scoreboard players add @s phys_veh.gear_num_max 1
execute if score #temp main_score matches ..-1 run scoreboard players remove @s phys_veh.gear_num_min 1

scoreboard players operation #temp2 main_score = #temp main_score

data remove storage midwut:main gears[0]
execute if data storage midwut:main gears[0] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/gears_loop