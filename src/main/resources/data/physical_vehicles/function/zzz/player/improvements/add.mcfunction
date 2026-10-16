tag @s add phys_veh.has_seat_improvements
execute on vehicle run scoreboard players operation #seat_id main_score = @s phys_veh.id.seat
scoreboard players operation @s phys_veh.id.seat = #seat_id main_score
execute on vehicle run data modify storage midwut:main SeatData set from entity @s data.phys_veh
function #physical_vehicles:seat_improvements/search_add