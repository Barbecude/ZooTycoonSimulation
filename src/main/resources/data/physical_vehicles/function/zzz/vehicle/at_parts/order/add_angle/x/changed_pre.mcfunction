execute if entity @s[tag=phys_veh.add_angle.x.have_sound] if score @s phys_veh.sound_timer.x_traverse matches 1.. run scoreboard players remove @s phys_veh.sound_timer.x_traverse 1
execute unless score @s phys_veh.add_angle.x = @s phys_veh.adding_angle.x run return run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle/x/changed
return fail