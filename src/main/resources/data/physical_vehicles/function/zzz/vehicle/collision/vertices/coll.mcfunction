execute if score #vp.x main_score matches 0 if score #vp.y main_score matches 0 if score #vp.z main_score matches 0 run return fail

scoreboard players operation #temp main_score = #pos.x main_score
execute store result storage midwut:main macros.x_s double 0.001 run scoreboard players operation #temp main_score -= #vp_dt.x main_score
scoreboard players operation #temp main_score = #pos.y main_score
execute store result storage midwut:main macros.y_s double 0.001 run scoreboard players operation #temp main_score -= #vp_dt.y main_score
scoreboard players operation #temp main_score = #pos.z main_score
execute store result storage midwut:main macros.z_s double 0.001 run scoreboard players operation #temp main_score -= #vp_dt.z main_score


tag @s remove phys_veh.test
execute unless score #vp.y main_score matches 0 if function physical_vehicles:zzz/vehicle/collision/vertices/y/y run function physical_vehicles:zzz/vehicle/collision/vertices/y/main
execute unless score #vp.x main_score matches 0 if function physical_vehicles:zzz/vehicle/collision/vertices/x/x run function physical_vehicles:zzz/vehicle/collision/vertices/x/main
execute unless score #vp.z main_score matches 0 if function physical_vehicles:zzz/vehicle/collision/vertices/z/z run function physical_vehicles:zzz/vehicle/collision/vertices/z/main
execute if entity @s[tag=phys_veh.test] run return 1
execute if function physical_vehicles:zzz/vehicle/collision/vertices/x/yx run return run function physical_vehicles:zzz/vehicle/collision/vertices/x/main
execute if function physical_vehicles:zzz/vehicle/collision/vertices/z/yxz run return run function physical_vehicles:zzz/vehicle/collision/vertices/z/main
