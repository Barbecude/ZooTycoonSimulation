data modify entity @s CustomName set from storage midwut:main Data.CustomName
execute as @e[type=item_display,tag=physical_vehicle,tag=phys_veh.model,tag=phys_veh.selected_part,tag=phys_veh.skin.jeb_,distance=..96] run function physical_vehicles:zzz/vehicle/change_name_to_jeb_model_remove
