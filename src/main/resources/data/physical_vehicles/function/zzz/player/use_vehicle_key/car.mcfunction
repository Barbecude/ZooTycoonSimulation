execute on vehicle if items entity @s armor.body * run return fail

tag @s add phys_veh.selected_player
function physical_vehicles:zzz/player/use_vehicle_key/car_
execute on vehicle run item modify entity @s armor.body midwut:set_1_count
tag @s remove phys_veh.selected_player