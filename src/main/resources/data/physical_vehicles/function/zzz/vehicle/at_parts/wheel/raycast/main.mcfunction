#> physical_vehicles:zzz/vehicle/at_parts/wheel/offset
function physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/1_block/pre
scoreboard players operation #distance main_score < @s phys_veh.tread_height
#tellraw @a [{score:{name:"#distance",objective:"main_score"}}]
scoreboard players operation #temp main_score = #distance main_score
scoreboard players operation #temp main_score -= @s phys_veh.wheel.suspension.offset
execute if score #temp main_score matches 1.. if score #temp main_score > @s phys_veh.max_delta run function physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/max_delta/p
#execute if score #temp main_score < @s phys_veh.max_delta_neg run function physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/max_delta/n
scoreboard players operation @s phys_veh.wheel.offset_y = #distance main_score
scoreboard players operation @s phys_veh.wheel.offset_y -= @s phys_veh.wheel.suspension.height
execute store result storage midwut:main macros.temp float 0.001 run scoreboard players operation @s phys_veh.wheel.offset_y < #0 const
function physical_vehicles:zzz/vehicle/at_parts/wheel/teleport with storage midwut:main macros


