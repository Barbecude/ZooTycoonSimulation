execute if entity @s[tag=!phys_veh.disable.offset] run function physical_vehicles:zzz/vehicle/at_parts/wheel/offset
execute if entity @s[tag=!phys_veh.wheel_no_spin] run function physical_vehicles:zzz/vehicle/at_parts/wheel/rotation_x
execute if entity @s[tag=phys_veh.wheel.steering] run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/rotation_steering
function physical_vehicles:zzz/vehicle/at_parts/rotation