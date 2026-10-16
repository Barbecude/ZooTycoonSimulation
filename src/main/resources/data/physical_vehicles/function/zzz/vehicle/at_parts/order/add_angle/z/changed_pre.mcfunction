execute if entity @s[tag=phys_veh.add_angle.z.have_sound] if score @s phys_veh.sound_timer.z_traverse matches 1.. run scoreboard players remove @s phys_veh.sound_timer.z_traverse 1
execute unless score @s phys_veh.add_angle.z = @s phys_veh.adding_angle.z run return run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/z/changed
return fail