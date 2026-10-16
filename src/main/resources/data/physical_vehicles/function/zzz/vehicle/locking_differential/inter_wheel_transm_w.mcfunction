scoreboard players operation @s phys_veh.transmission_w /= #10 const
execute if entity @s[tag=phys_veh.tracked_control] unless entity @s[scores={phys_veh.control.brake_a=0,phys_veh.control.brake_d=0}] unless score @s phys_veh.current_gear matches 0 run function physical_vehicles:zzz/vehicle/locking_differential/track_add_w
scoreboard players operation @s phys_veh.transmission_w /= #c main_score
