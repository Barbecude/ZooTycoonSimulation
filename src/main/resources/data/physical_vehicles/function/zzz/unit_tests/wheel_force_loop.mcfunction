scoreboard players operation @s phys_veh.spin.x = #mspin main_score
function physical_vehicles:zzz/unit_tests/wheel_force_main

execute store result storage midwut:main macros.x float -0.0004 run scoreboard players get #mspin main_score
execute store result storage midwut:main macros.y float 0.01 run scoreboard players get #fy2 main_score
function physical_vehicles:zzz/unit_tests/wheel_force_spawn with storage midwut:main macros

scoreboard players add #mspin main_score 10
execute if score #mspin main_score matches -10000..10000 run function physical_vehicles:zzz/unit_tests/wheel_force_loop