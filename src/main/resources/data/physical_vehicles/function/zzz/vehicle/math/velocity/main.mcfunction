execute unless score @s phys_veh.velocity.x matches 0 run function physical_vehicles:zzz/vehicle/math/velocity/add/x
execute unless score @s phys_veh.velocity.y matches 0 run function physical_vehicles:zzz/vehicle/math/velocity/add/y
execute unless score @s phys_veh.velocity.z matches 0 run function physical_vehicles:zzz/vehicle/math/velocity/add/z
data modify entity @s Pos set from storage midwut:main Data.Pos
