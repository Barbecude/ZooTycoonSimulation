scoreboard players operation #temp main_score = @s phys_veh.bounding_box.x
scoreboard players remove #temp main_score 50
execute if score #temp main_score matches 1.. run scoreboard players operation #temp main_score *= #20 const
scoreboard players add #temp main_score 50
scoreboard players operation #temp main_score /= #50 const
scoreboard players operation #temp main_score > #1 const
scoreboard players set @s phys_veh.afk_timer 10000
scoreboard players operation @s phys_veh.afk_timer /= #temp main_score


execute store result storage midwut:main macros.w float -0.05 run scoreboard players operation #width main_score = @s phys_veh.bounding_box.x
scoreboard players remove #width main_score 10
execute if score #width main_score matches ..-1 run scoreboard players set #width main_score 0
scoreboard players add #width main_score 1
data modify storage midwut:main road_maker_data set value []
