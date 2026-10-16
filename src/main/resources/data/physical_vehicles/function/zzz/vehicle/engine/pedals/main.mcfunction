function physical_vehicles:zzz/vehicle/engine/pedals/clutch_pedal
execute unless score @s phys_veh.control.clutch matches 0..100 run function physical_vehicles:zzz/vehicle/engine/pedals/clutch_pedal_norm
function physical_vehicles:zzz/vehicle/engine/pedals/brake_pedal
execute unless score @s phys_veh.control.brake matches 0..100 run function physical_vehicles:zzz/vehicle/engine/pedals/brake_pedal_norm
function physical_vehicles:zzz/vehicle/engine/pedals/throttle_pedal
execute unless score @s phys_veh.control.throttle matches 0..100 run function physical_vehicles:zzz/vehicle/engine/pedals/throttle_pedal_norm
execute if entity @s[tag=phys_veh.tracked_control] run function physical_vehicles:zzz/vehicle/engine/pedals/tracks