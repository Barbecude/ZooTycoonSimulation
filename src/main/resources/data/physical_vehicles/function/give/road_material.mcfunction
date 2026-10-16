# material
# pixel
# colors

$execute if score #0 const matches $(pixel) run return fail
$execute if score #16 const matches ..$(pixel) run return run give @s $(material)

$data modify storage midwut:main macros merge value {material:$(material),pixel:$(pixel),colors:$(colors)}
execute store result storage midwut:main macros.height double 0.0625 run data get storage midwut:main macros.pixel
function physical_vehicles:zzz/block/give_road_macros with storage midwut:main macros

