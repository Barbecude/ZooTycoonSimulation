data modify storage midwut:main temp set value {"type":"minecraft:single_option","key":"phys_veh_transfer","label":{"translate":"phys_veh.vehicle_dashboard.transfer"},"options":[]}
data modify storage midwut:main temp.options set from storage midwut:phys_veh Data.data.phys_veh.transmission.transfers
execute store result storage midwut:main macros.temp int 1 run scoreboard players get @s phys_veh.current_transfer_index
function physical_vehicles:zzz/vehicle/vehicle_dashboard/transfers_initial with storage midwut:main macros
data modify storage midwut:phys_veh vehicle_dashboard prepend from storage midwut:main temp