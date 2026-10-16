scoreboard players operation #sum_radius main_score = @s phys_veh.collision_box.radius
execute store result storage midwut:main macros.distance float 0.001 run scoreboard players operation #sum_radius main_score += #collision_radius_0 main_score
function physical_vehicles:zzz/vehicle/collision/dynamic/radius_check_macro with storage midwut:main macros
