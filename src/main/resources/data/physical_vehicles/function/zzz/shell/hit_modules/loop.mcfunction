data modify storage midwut:main module set from storage midwut:main boxes[-1].modules[-1]
function physical_vehicles:zzz/shell/hit_modules/module_choice
data remove storage midwut:main boxes[-1].modules[-1]
execute if data storage midwut:main boxes[-1].modules[0] run function physical_vehicles:zzz/shell/hit_modules/loop