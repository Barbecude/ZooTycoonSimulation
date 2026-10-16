scoreboard players operation #temp main_score = #vf main_score
execute if score #temp main_score matches ..-1 run scoreboard players operation #temp main_score *= #-1 const
scoreboard players operation #temp main_score *= #100 const
scoreboard players operation #temp main_score /= #350 const

execute if score #temp main_score matches 0..1 run scoreboard players set #temp main_score 2
function physical_vehicles:zzz/vehicle/at_parts/wheel/decrease_fy_
