execute if entity @s[tag=!phys_veh.disable.offset] run function physical_vehicles:zzz/vehicle/at_parts/offset
execute if entity @s[tag=!phys_veh.disable.rotation] run function physical_vehicles:zzz/vehicle/at_parts/seat_rotation

execute on passengers run function physical_vehicles:zzz/player/driver/main

execute if entity @s[name="jeb_"] run return run function physical_vehicles:zzz/vehicle/at_parts/seat/jeb_
execute if data entity @s CustomName run function physical_vehicles:zzz/vehicle/at_parts/seat/name

execute if items entity @s[tag=!phys_veh.ignition] armor.body *[minecraft:custom_data~{phys_veh.car_key:true}] run function physical_vehicles:zzz/player/driver/ignition/on_seat
execute unless entity @s[tag=!phys_veh.ignition,tag=!phys_veh.has_key] unless items entity @s armor.body *[minecraft:custom_data~{phys_veh.car_key:true}] run function physical_vehicles:zzz/player/driver/ignition/off
