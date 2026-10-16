execute if entity @s[tag=phys_veh.hand_brake] run return run function physical_vehicles:zzz/player/driver/hand_brake_remove
tag @s add phys_veh.hand_brake
function physical_vehicles:zzz/vehicle/other/sound_8 with storage midwut:main Data.data.phys_veh.sounds[{id:"handbrake_tightened"}]