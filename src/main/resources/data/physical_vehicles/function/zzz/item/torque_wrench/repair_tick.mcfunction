scoreboard players add @s phys_veh.track_repair_timer 1
scoreboard players operation #theshold main_score = @s phys_veh.track_repair_timer
function physical_vehicles:zzz/vehicle/other/sound_neutral_32_periodic_tick with entity @s data.phys_veh.servicing_sound
execute if score @s phys_veh.track_repair_timer >= @s phys_veh.track_repair_time run function physical_vehicles:zzz/item/torque_wrench/repair