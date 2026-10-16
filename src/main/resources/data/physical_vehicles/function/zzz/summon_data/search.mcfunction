## Cars
execute if entity @s[tag=phys_veh.armored_truck_4x4] run return run function physical_vehicles:zzz/summon_data/vehicle/armored_truck_4x4
execute if entity @s[tag=phys_veh.jeep_cj-5_1964] run return run function physical_vehicles:summon/jeep_cj-5_1964
execute if entity @s[tag=phys_veh.ecto-1a] run return run function physical_vehicles:summon/ecto-1a
execute if entity @s[tag=phys_veh.hammer_monster] run return run function physical_vehicles:summon/hammer_monster
execute if entity @s[tag=phys_veh.oldsmobile_cutlass_supreme_coupe_1980] run return run function physical_vehicles:summon/oldsmobile_cutlass_supreme_coupe_1980
execute if entity @s[tag=phys_veh.tt-66052] run return run function physical_vehicles:summon/tt-66052
execute if entity @s[tag=phys_veh.vanna] run return run function physical_vehicles:summon/vanna
execute if entity @s[tag=phys_veh.buhanka] run return run function physical_vehicles:summon/buhanka

### By Falanta
execute if entity @s[tag=phys_veh.zaz-968a] run return run function physical_vehicles:zzz/summon_data/vehicle/zaz-968a
execute if entity @s[tag=phys_veh.zaz-968m] run return run function physical_vehicles:zzz/summon_data/vehicle/zaz-968m
execute if entity @s[tag=phys_veh.zil-130] run return run function physical_vehicles:summon/zil-130

## Others
execute if entity @s[tag=phys_veh.remover] run return run function physical_vehicles:zzz/item/remover/main
kill @e[type=#physical_vehicles:refers_to_vehicle/all,distance=..96,tag=get_id]
