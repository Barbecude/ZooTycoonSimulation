tag @s add phys_veh.has_seat_improvements.blast_protection

$execute unless items entity @s armor.body * run return run item replace entity @s armor.body with minecraft:poisonous_potato[minecraft:custom_data={phys_veh.seat_improvements:true},minecraft:enchantments={"minecraft:blast_protection":$(blast_protection)},minecraft:equippable={slot:"body"},!minecraft:food,!minecraft:consumable]
item modify entity @s armor.body physical_vehicles:improvements/blast_protection_add