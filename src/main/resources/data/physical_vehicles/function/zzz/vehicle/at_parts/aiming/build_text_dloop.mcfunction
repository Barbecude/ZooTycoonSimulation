data modify storage midwut:main macros.temp set from storage midwut:main temp[0].translate
function physical_vehicles:zzz/vehicle/at_parts/aiming/build_text_dm with storage midwut:main macros
data modify storage midwut:main aiming.builded_title append from storage midwut:main temp[0]

data remove storage midwut:main temp[0]
execute if data storage midwut:main temp[0] run function physical_vehicles:zzz/vehicle/at_parts/aiming/build_text_dloop