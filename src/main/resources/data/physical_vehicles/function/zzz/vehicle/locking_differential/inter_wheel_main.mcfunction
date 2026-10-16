scoreboard players set #wheel_axle_id main_score 0
scoreboard players set #c main_score 0
function physical_vehicles:zzz/vehicle/locking_differential/inter_wheel_loop
execute if entity @s[tag=!phys_veh.locking_center_differential] run function physical_vehicles:zzz/vehicle/locking_differential/inter_wheel_transm_w
