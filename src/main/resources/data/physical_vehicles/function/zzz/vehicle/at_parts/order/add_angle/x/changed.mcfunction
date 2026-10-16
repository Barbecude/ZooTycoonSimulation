scoreboard players operation @s phys_veh.adding_angle.x = @s phys_veh.add_angle.x
scoreboard players operation #phys_veh.angle_x_change main_score = @s phys_veh.add_angle.x
execute if entity @s[tag=phys_veh.add_angle.x.have_sound] unless score @s phys_veh.sound_timer.x_traverse matches 1.. run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/x/sound with entity @s data.phys_veh.x_traverse_sound

return 1