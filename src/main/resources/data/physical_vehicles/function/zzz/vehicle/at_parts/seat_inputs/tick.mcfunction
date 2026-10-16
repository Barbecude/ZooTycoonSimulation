execute as @e[type=minecraft:llama,tag=phys_veh.llama_seat_inputs,tag=phys_veh.selected_part,distance=..96] run function physical_vehicles:zzz/vehicle/at_parts/seat_inputs/main

execute if entity @s[tag=phys_veh.have_input.0] run function physical_vehicles:zzz/vehicle/inputs/0/main
execute if entity @s[tag=phys_veh.have_input.1] run function physical_vehicles:zzz/vehicle/inputs/1/main
execute if entity @s[tag=phys_veh.have_input.2] run function physical_vehicles:zzz/vehicle/inputs/2/main
execute if entity @s[tag=phys_veh.have_input.3] run function physical_vehicles:zzz/vehicle/inputs/3/main
execute if entity @s[tag=phys_veh.have_input.4] run function physical_vehicles:zzz/vehicle/inputs/4/main
