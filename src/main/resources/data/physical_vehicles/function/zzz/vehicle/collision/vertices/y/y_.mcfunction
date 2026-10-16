execute if entity @s[tag=phys_veh.debug.particles] run function physical_vehicles:zzz/vehicle/collision/vertices/particles_coll with storage midwut:main macros
$execute positioned 0.0 0.0 0.0 positioned ~$(x_s) ~$(y_f) ~$(z_s) unless function midwut:help/check/air_only_collision run return run function physical_vehicles:zzz/vehicle/collision/vertices/get_fric_coeff
return fail