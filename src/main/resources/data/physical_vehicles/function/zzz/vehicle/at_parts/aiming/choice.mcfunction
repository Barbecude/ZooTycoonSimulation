tag @s add phys_veh.aiming_control_pressed
scoreboard players operation #object_id main_score = @s phys_veh.id.seat
execute if entity @s[tag=phys_veh.is_aiming] run return run function physical_vehicles:zzz/vehicle/at_parts/aiming/disable
function physical_vehicles:zzz/vehicle/at_parts/aiming/enable