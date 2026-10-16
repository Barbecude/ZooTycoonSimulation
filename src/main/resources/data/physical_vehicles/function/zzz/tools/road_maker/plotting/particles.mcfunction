#function physical_vehicles:zzz/tools/road_maker/plotting/particles
execute positioned as @e[type=minecraft:marker,tag=phys_veh.road_maker.block] run particle minecraft:dust{color:[1f,0f,0f],scale:0.2f} ~ ~ ~
execute positioned as @e[type=minecraft:marker,tag=phys_veh.road_maker.temp_marker2] run particle minecraft:dust{color:[0f,1f,0f],scale:0.2f} ~ ~ ~
execute positioned as @e[type=minecraft:marker,tag=phys_veh.road_maker.temp_marker] run particle minecraft:dust{color:[0f,0f,1f],scale:0.2f} ~ ~ ~