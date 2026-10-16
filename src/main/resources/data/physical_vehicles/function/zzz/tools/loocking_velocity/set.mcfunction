tag @n[type=minecraft:text_display,tag=phys_veh.main] add phys_veh.selected_part
execute as 0000051d-0000-0fb7-0000-001500000014 in midwut:main positioned 0.0 0.0 0.0 run tp @s ~ ~ ~
$execute as 0000051d-0000-0fb7-0000-001500000014 in midwut:main positioned 0.0 0.0 0.0 positioned ^ ^ ^$(strength) run function physical_vehicles:zzz/tools/loocking_velocity/motion
tag @e[type=minecraft:text_display,tag=phys_veh.main,tag=phys_veh.selected_part] remove phys_veh.selected_part