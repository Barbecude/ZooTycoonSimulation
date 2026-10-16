#execute if entity @s[tag=phys_veh.have_on_board_clutch_lock] unless score #phys_veh.control.brake main_score matches 0 run return run scoreboard players set #wheel_acc main_score -1
execute if score #phys_veh.control.brake_a main_score matches 1.. if score @s phys_veh.id.wheel_axle = @s phys_veh.track_brake.a run function physical_vehicles:zzz/vehicle/at_parts/wheel/braking_a
execute if score #phys_veh.control.brake_d main_score matches 1.. if score @s phys_veh.id.wheel_axle = @s phys_veh.track_brake.d run function physical_vehicles:zzz/vehicle/at_parts/wheel/braking_d
