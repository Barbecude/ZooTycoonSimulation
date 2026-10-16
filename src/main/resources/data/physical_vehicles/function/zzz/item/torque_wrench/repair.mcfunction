scoreboard players set @s phys_veh.track_repair_timer 0
tag @s remove phys_veh.track_broken
function physical_vehicles:zzz/vehicle/other/sound_neutral_32 with entity @s data.phys_veh.repair_sound