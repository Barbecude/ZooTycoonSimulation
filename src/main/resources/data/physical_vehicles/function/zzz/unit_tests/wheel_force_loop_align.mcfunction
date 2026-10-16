scoreboard players operation @s phys_veh.spin.x = #mspin main_score
function physical_vehicles:zzz/unit_tests/wheel_force_main

execute store result storage midwut:main macros.x float -0.001 run scoreboard players get #main.velocity.x main_score
execute store result storage midwut:main macros.y float -0.01 run scoreboard players get #fz2 main_score
function physical_vehicles:zzz/unit_tests/wheel_force_spawn with storage midwut:main macros

scoreboard players add #main.velocity.x main_score 5
scoreboard players add #main.last_velocity.x main_score 5
execute unless score #main.velocity.x main_score matches 4000.. run function physical_vehicles:zzz/unit_tests/wheel_force_loop_align