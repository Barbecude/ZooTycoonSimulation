tag @s remove phys_veh.has_seat_improvements
scoreboard players reset @s phys_veh.id.seat
execute if entity @s[tag=phys_veh.in_aiming] run function physical_vehicles:zzz/vehicle/at_parts/aiming/disable_player
function #physical_vehicles:seat_improvements/search_remove