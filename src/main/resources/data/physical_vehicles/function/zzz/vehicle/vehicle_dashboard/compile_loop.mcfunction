data modify storage midwut:main macros.temp set from storage midwut:main temp[-1].key
function physical_vehicles:zzz/vehicle/vehicle_dashboard/compile_macro with storage midwut:main macros
data remove storage midwut:main temp[-1]
execute if data storage midwut:main temp[0] run function physical_vehicles:zzz/vehicle/vehicle_dashboard/compile_loop