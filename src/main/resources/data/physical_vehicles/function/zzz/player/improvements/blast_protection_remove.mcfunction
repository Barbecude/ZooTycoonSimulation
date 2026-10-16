tag @s remove phys_veh.has_seat_improvements.blast_protection
item modify entity @s armor.body physical_vehicles:improvements/blast_protection_remove
execute if items entity @s armor.body *[minecraft:custom_data~{phys_veh.seat_improvements:true},!minecraft:enchantments~[{}]] run return run item replace entity @s armor.body with minecraft:air