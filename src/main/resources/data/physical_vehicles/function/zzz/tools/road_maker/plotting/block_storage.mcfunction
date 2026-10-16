#$execute unless data storage midwut:main road_maker_data[{x:$(px),y:$(py),z:$(pz)}] run return run function physical_vehicles:zzz/tools/road_maker/plotting/block_storage_new with storage midwut:main macros
execute unless entity @e[type=minecraft:marker,tag=phys_veh.road_maker.block,limit=1,distance=..0.1] summon minecraft:marker run function physical_vehicles:zzz/tools/road_maker/plotting/block_storage_new
execute as @e[type=minecraft:marker,tag=phys_veh.road_maker.block,limit=1,distance=..0.1] run function physical_vehicles:zzz/tools/road_maker/plotting/block_storage_add
