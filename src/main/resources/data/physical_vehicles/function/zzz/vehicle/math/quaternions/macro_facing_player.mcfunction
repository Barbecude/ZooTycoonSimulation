$execute positioned 0.0 0.0 0.0 facing ~$(x) ~$(y) ~$(z) run function physical_vehicles:zzz/vehicle/other/rotate_s
data modify storage midwut:main macros.ey set from entity @s Rotation[0]
data modify storage midwut:main macros.ex set from entity @s Rotation[1]
function physical_vehicles:zzz/vehicle/other/rotate_s_macro with storage midwut:main macros