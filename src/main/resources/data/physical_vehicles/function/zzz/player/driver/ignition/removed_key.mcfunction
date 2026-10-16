tag @s remove phys_veh.has_key
tag @e[type=minecraft:text_display,tag=phys_veh.main,tag=phys_veh.selected_part,limit=1] remove phys_veh.has_key
execute positioned as @s run playsound physical_vehicles:other.car_key_remove_from_ignition ambient @a[distance=..32] ~ ~ ~ 0.5 1