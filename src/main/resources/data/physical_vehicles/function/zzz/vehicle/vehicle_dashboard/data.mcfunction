data modify storage midwut:phys_veh Data set from entity @s

function #physical_vehicles:vehicle_dashboard

execute if entity @s[tag=phys_veh.have_transfers] run function physical_vehicles:zzz/vehicle/vehicle_dashboard/transfers
execute if entity @s[tag=phys_veh.have_locking_inter_wheel_differential] run function physical_vehicles:zzz/vehicle/vehicle_dashboard/locking_inter_wheel_differential
execute if entity @s[tag=phys_veh.have_locking_center_differential] run function physical_vehicles:zzz/vehicle/vehicle_dashboard/locking_center_differential
