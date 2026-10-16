execute if entity @s[tag=!phys_veh.have_input_interaction] run return run execute if predicate midwut:check/passenger/player run function physical_vehicles:zzz/vehicle/at_parts/seat_inputs/interaction_add
execute unless predicate midwut:check/passenger/player run return run function physical_vehicles:zzz/vehicle/at_parts/seat_inputs/interaction_remove
execute on passengers if entity @s[tag=phys_veh.input_interaction] run function physical_vehicles:zzz/vehicle/at_parts/seat_inputs/interaction_tick
