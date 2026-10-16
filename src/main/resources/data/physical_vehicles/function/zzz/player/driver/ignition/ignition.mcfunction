execute if entity @s[tag=!phys_veh.has_key] positioned as @e[type=minecraft:text_display,tag=phys_veh.hp,tag=phys_veh.selected_part,distance=..96] run function physical_vehicles:zzz/player/driver/ignition/inserted_key with storage midwut:main Data.data.phys_veh.sounds[{id:"ignition_start"}]

execute if function physical_vehicles:zzz/player/driver/ignition/check_idle_w run return run tag @s add phys_veh.ignition
execute if score @s phys_veh.ignition_sound_timer matches 0 positioned as @e[type=minecraft:text_display,tag=phys_veh.hp,tag=phys_veh.selected_part,distance=..96] run function physical_vehicles:zzz/player/driver/ignition/starting_sound with storage midwut:main Data.data.phys_veh.sounds[{id:"ignition_starting"}]
scoreboard players remove @s phys_veh.ignition_sound_timer 1
execute as @e[type=minecraft:text_display,tag=phys_veh.main,tag=phys_veh.selected_part,limit=1,x=0] run function physical_vehicles:zzz/player/driver/ignition/starting_engine