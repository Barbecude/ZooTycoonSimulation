scoreboard players operation @s phys_veh.adding_angle.z = @s phys_veh.add_angle.z
scoreboard players operation #phys_veh.angle_z_change main_score = @s phys_veh.add_angle.z
execute if entity @s[tag=phys_veh.add_angle.z.have_sound] unless score @s phys_veh.sound_timer.z_traverse matches 1.. run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/z/sound_pre with entity @s data.phys_veh

return 1