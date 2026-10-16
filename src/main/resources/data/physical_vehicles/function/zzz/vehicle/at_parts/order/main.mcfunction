execute if score @s phys_veh.id.object_parent = @s phys_veh.id.object_parent run scoreboard players operation #object_parent_id main_score = @s phys_veh.id.object_parent
function physical_vehicles:zzz/vehicle/at_parts/order/offset_choice
execute if entity @s[tag=!phys_veh.disable.rotation,type=llama,predicate=midwut:check/periodic_tick/2] run function physical_vehicles:zzz/vehicle/at_parts/order/seat_rotate
execute if entity @s[tag=!phys_veh.disable.rotation,tag=!phys_veh.seat,type=#midwut:not_living/entity_display] run function physical_vehicles:zzz/vehicle/at_parts/order/rotation
execute if entity @s[type=minecraft:llama,tag=phys_veh.have_seat_improvements] on passengers if entity @s[type=#midwut:living/all,tag=!phys_veh.has_seat_improvements] run function physical_vehicles:zzz/player/improvements/main
execute if entity @s[tag=phys_veh.have_parent_entity_rotation] run function physical_vehicles:zzz/vehicle/at_parts/order/parent_entity_rotation
execute if entity @s[tag=phys_veh.model,tag=phys_veh.skin.jeb_] run function physical_vehicles:zzz/vehicle/at_parts/jeb_colors
execute if entity @s[tag=phys_veh.cannon] run function physical_vehicles:zzz/vehicle/at_parts/cannon/main
execute if entity @s[tag=phys_veh.aiming] run function physical_vehicles:zzz/vehicle/at_parts/aiming/main