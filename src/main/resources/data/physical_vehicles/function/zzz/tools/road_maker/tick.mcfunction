execute as @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=!phys_veh.road_maker.spawn] run function physical_vehicles:zzz/tools/road_maker/main
execute as @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=!phys_veh.road_maker.spawn,tag=phys_veh.road_maker.first,predicate=physical_vehicles:timer_10] run function physical_vehicles:zzz/tools/road_maker/plotting/pre

execute positioned as @e[type=minecraft:marker,tag=phys_veh.road_maker.temp_marker] run particle minecraft:dust{color:[0f,0f,1f],scale:0.2f} ~ ~ ~
execute positioned as @e[type=minecraft:marker,tag=phys_veh.road_maker.block] run particle minecraft:dust{color:[1f,0f,0f],scale:0.2f} ~ ~ ~

execute if entity @e[type=minecraft:item_display,tag=phys_veh.road_maker.main,tag=!phys_veh.road_maker.spawn] run schedule function physical_vehicles:zzz/tools/road_maker/tick 1t replace