#> physical_vehicles:zzz/unit_tests/wheel_force
execute if entity @s[type=minecraft:player] run return fail

#tag @s add phys_veh.wheel.steering

function physical_vehicles:zzz/unit_tests/ford_mustang

kill @e[tag=wheel_force_test2]
tag @e[tag=wheel_force_test] add wheel_force_test2
function physical_vehicles:zzz/unit_tests/wheel_force_loop
#function physical_vehicles:zzz/unit_tests/wheel_force_loop_lateral
#function physical_vehicles:zzz/unit_tests/wheel_force_loop_align

kill @s