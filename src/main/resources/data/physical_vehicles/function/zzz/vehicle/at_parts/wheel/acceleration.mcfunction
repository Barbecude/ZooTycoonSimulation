execute if entity @s[tag=!phys_veh.wheel.driving] run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/acceleration_brake

scoreboard players set #wheel_acc main_score 0
execute store result score #sign main_score if score @s phys_veh.spin.x matches 1..

scoreboard players operation #wheel_acc main_score = #engine_transmission_torque main_score
#execute if entity @s[tag=!phys_veh.wheel.driving] run tellraw @a [{score:{name:"#wheel_acc",objective:"main_score"}}]
execute if entity @s[tag=phys_veh.have_on_board_clutch_lock] unless score #phys_veh.control.brake main_score matches 0 if score #phys_veh.control.brake_a main_score matches 1.. if score @s phys_veh.id.wheel_axle = @s phys_veh.track_brake.a run scoreboard players operation #wheel_acc main_score *= #-1 const
execute if entity @s[tag=phys_veh.have_on_board_clutch_lock] unless score #phys_veh.control.brake main_score matches 0 if score #phys_veh.control.brake_d main_score matches 1.. if score @s phys_veh.id.wheel_axle = @s phys_veh.track_brake.d run scoreboard players operation #wheel_acc main_score *= #-1 const

execute if score #sign main_score matches 1 run scoreboard players operation #wheel_acc main_score -= #brakes_torque main_score
execute if score #sign main_score matches 0 run scoreboard players operation #wheel_acc main_score += #brakes_torque main_score


scoreboard players operation #wheel_acc main_score *= #1000 const
scoreboard players operation #wheel_acc main_score /= @s phys_veh.moment_of_inertia.x
scoreboard players operation @s phys_veh.spin.x += #wheel_acc main_score

execute if score @s phys_veh.spin.x matches 1.. run return run execute if score #sign main_score matches 0 if score #brakes_torque main_score matches 1.. run scoreboard players set @s phys_veh.spin.x 0
execute if score @s phys_veh.spin.x matches ..-1 run return run execute if score #sign main_score matches 1 if score #brakes_torque main_score matches 1.. run scoreboard players set @s phys_veh.spin.x 0

