scoreboard players operation #seat_id main_score = @s phys_veh.id.seat
execute if entity @s[tag=phys_veh.has_seat_improvements] unless function physical_vehicles:zzz/player/improvements/check_old_id run function physical_vehicles:zzz/player/dismount_seat/main
execute if entity @s[tag=!phys_veh.has_seat_improvements] run function physical_vehicles:zzz/player/improvements/add
