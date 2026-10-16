scoreboard players operation @s phys_veh.adding_angle.y = @s phys_veh.add_angle.y
scoreboard players operation #phys_veh.angle_y_change main_score = @s phys_veh.add_angle.y
execute if entity @s[tag=phys_veh.add_angle.y.have_sound] unless score @s phys_veh.sound_timer.y_traverse matches 1.. run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/y/sound with entity @s data.phys_veh.y_traverse_sound

return 1