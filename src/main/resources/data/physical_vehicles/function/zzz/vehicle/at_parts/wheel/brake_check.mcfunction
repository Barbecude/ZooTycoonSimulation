execute unless score #phys_veh.control.brake main_score matches 0 if function physical_vehicles:zzz/vehicle/at_parts/wheel/braking run return 1
execute if entity @s[tag=phys_veh.tracked_control] unless score #wheel_angle main_score matches 0 run function physical_vehicles:zzz/vehicle/at_parts/wheel/track_brake
