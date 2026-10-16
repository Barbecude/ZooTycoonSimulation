execute on passengers run tag @s add phys_veh.selected_passenger
execute on passengers run ride @s dismount

tag @s add phys_veh.have_input_interaction
summon minecraft:interaction ~ ~ ~ {Tags:["get_id","phys_veh.input_interaction"],height:1.125f,width:0.25f,response:false}
ride @e[type=minecraft:interaction,tag=get_id,limit=1] mount @s
tag @e[type=minecraft:interaction,tag=get_id,limit=1] remove get_id

tag @s add phys_veh.selected_vehicle
execute positioned as @s as @e[tag=phys_veh.selected_passenger,distance=..5] run function physical_vehicles:zzz/vehicle/at_parts/seat_inputs/back_ride
tag @s remove phys_veh.selected_vehicle