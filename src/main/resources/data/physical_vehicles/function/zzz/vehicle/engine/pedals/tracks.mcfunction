function physical_vehicles:zzz/vehicle/engine/pedals/brake_d_pedal
execute unless score @s phys_veh.control.brake_d matches 0..100 run function physical_vehicles:zzz/vehicle/engine/pedals/brake_d_pedal_norm
function physical_vehicles:zzz/vehicle/engine/pedals/brake_a_pedal
execute unless score @s phys_veh.control.brake_a matches 0..100 run function physical_vehicles:zzz/vehicle/engine/pedals/brake_a_pedal_norm