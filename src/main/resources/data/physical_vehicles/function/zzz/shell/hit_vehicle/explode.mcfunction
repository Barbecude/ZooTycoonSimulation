tag @s add phys_veh.shell.hit
tag @s add phys_veh.shell.hit_vehicle
execute if entity @s[tag=phys_veh.shell.hit_sound] if entity @a[distance=..64,limit=1] run function physical_vehicles:zzz/shell/hit/sound with storage midwut:main DataShell.data.phys_veh.shell.hit_sound
execute if entity @s[tag=phys_veh.shell.explode_on_impact] run function physical_vehicles:zzz/shell/hit/explode_on_impact_block with storage midwut:main DataShell.data.phys_veh.shell