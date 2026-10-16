scoreboard players set #input main_score 0
execute if score @s phys_veh.id.input matches 0 run function physical_vehicles:zzz/vehicle/at_parts/aiming/input0
execute if score @s phys_veh.id.input matches 1 run function physical_vehicles:zzz/vehicle/at_parts/aiming/input1
execute if score @s phys_veh.id.input matches 2 run function physical_vehicles:zzz/vehicle/at_parts/aiming/input2
execute if score @s phys_veh.id.input matches 3 run function physical_vehicles:zzz/vehicle/at_parts/aiming/input3
execute if score @s phys_veh.id.input matches 4 run function physical_vehicles:zzz/vehicle/at_parts/aiming/input4

execute if entity @s[tag=phys_veh.is_aiming] run function physical_vehicles:zzz/vehicle/at_parts/aiming/tick
execute if score #input main_score matches 1 if entity @s[tag=!phys_veh.aiming_control_pressed] run return run function physical_vehicles:zzz/vehicle/at_parts/aiming/choice
execute if entity @s[tag=phys_veh.aiming_control_pressed] if score #input main_score matches 0 run tag @s remove phys_veh.aiming_control_pressed