scoreboard players operation @s phys_veh.transmission_w += @e[type=#physical_vehicles:refers_to_vehicle/wheel,tag=phys_veh.wheel.driving,tag=phys_veh.selected_part,distance=..96] phys_veh.spin.x
scoreboard players operation @s phys_veh.transmission_w /= #1000 const
scoreboard players operation @s phys_veh.transmission_w /= @s phys_veh.count_driving_wheels
