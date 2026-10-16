tag @s remove phys_veh.has_seat_improvements.immunity
item modify entity @s armor.body physical_vehicles:improvements/immunity_remove
execute if items entity @s armor.body *[minecraft:custom_data~{phys_veh.seat_improvements:true},!minecraft:enchantments~[{}]] run return run item replace entity @s armor.body with minecraft:air