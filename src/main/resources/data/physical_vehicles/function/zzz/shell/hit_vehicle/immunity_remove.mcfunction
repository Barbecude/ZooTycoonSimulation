scoreboard players set @s phys_veh.seat_improvements_disable_timer 5
tag @s add phys_veh.seat_improvements_disable
execute if entity @s[tag=phys_veh.has_seat_improvements.immunity] run function physical_vehicles:zzz/player/improvements/immunity_remove