tag @s add phys_veh.has_key
tag @e[type=minecraft:text_display,tag=phys_veh.main,tag=phys_veh.selected_part,limit=1] add phys_veh.has_key
playsound physical_vehicles:other.car_key_insert_into_ignition ambient @a[distance=..32] ~ ~ ~ 0.5 1
$playsound $(sound_id) ambient @a[distance=..96] ~ ~ ~ $(volume) $(pitch)
$scoreboard players set @s phys_veh.ignition_sound_timer $(duration)
