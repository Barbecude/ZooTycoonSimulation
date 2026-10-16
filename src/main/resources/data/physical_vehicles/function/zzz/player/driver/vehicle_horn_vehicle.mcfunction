
data modify storage midwut:main macros.sound set from entity @s data.phys_veh.sounds[{id:"horn"}]
execute positioned as @s run function physical_vehicles:zzz/player/driver/vehicle_horn_engine with storage midwut:main macros.sound