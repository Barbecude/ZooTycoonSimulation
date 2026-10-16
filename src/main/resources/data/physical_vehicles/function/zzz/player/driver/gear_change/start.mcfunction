tag @s add phys_veh.gear_changing
scoreboard players operation @s phys_veh.gear_changing_timer = @s phys_veh.gear_changing_cooldown
$execute positioned as @s run playsound $(sound_id) ambient @a[distance=..8] ~ ~ ~ $(volume) $(pitch)