tag @s remove phys_veh.gear_changing
scoreboard players set @s phys_veh.gear_changing_timer 0
$execute positioned as @s run playsound $(sound_id) ambient @a[distance=..8] ~ ~ ~ $(volume) $(pitch)
