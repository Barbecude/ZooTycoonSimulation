#> physical_vehicles:zzz/vehicle/at_parts/wheel/main
scoreboard players operation #distance main_score = @s phys_veh.wheel.suspension.height
scoreboard players operation #distance main_score += @s phys_veh.radius
tag @s remove phys_veh.wheel.on_ground
tag @s remove phys_veh.test
#data modify storage midwut:main Data.item set from entity @s item
execute positioned as @s run function physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/main
#scoreboard players set @s phys_veh.wheel.suspension.force 0
scoreboard players set #wheel_acc main_score 0
scoreboard players set #brakes_torque main_score 0
execute if entity @s[tag=phys_veh.wheel.hand_brake] if score #hand_brake main_score matches 1 run function physical_vehicles:zzz/vehicle/at_parts/wheel/hand_brake
execute if entity @s[tag=phys_veh.wheel.driving] unless score #engine_transmission_torque main_score matches 0 run scoreboard players set #wheel_acc main_score 1
execute if entity @s[tag=phys_veh.wheel.braking] run function physical_vehicles:zzz/vehicle/at_parts/wheel/brake_check
execute if entity @s[tag=phys_veh.wheel.on_ground] run function physical_vehicles:zzz/vehicle/at_parts/wheel/on_ground
scoreboard players operation @s phys_veh.wheel.suspension.offset = #distance main_score

execute unless score #wheel_acc main_score matches 0 run function physical_vehicles:zzz/vehicle/at_parts/wheel/acceleration
execute unless score @s phys_veh.spin.x matches 0 run function physical_vehicles:zzz/vehicle/at_parts/wheel/decrease_spin/0

