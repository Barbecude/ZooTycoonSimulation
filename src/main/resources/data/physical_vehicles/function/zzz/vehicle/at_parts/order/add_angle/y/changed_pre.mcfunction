execute if entity @s[tag=phys_veh.add_angle.y.have_sound] if score @s phys_veh.sound_timer.y_traverse matches 1.. run scoreboard players remove @s phys_veh.sound_timer.y_traverse 1
execute unless score @s phys_veh.add_angle.y = @s phys_veh.adding_angle.y run return run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/y/changed
return fail