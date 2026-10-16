
scoreboard players operation #vl.z main_score = #main.rotated_spin.x main_score
scoreboard players operation #vl.x main_score = #main.rotated_spin.y main_score
scoreboard players operation #vl.y main_score = #main.rotated_spin.z main_score

scoreboard players operation #vl.x main_score *= @s phys_veh.offset2.z
scoreboard players operation #temp main_score = #vl.y main_score
scoreboard players operation #temp main_score *= @s phys_veh.offset2.y
scoreboard players operation #vl.x main_score -= #temp main_score
scoreboard players operation #vl.x main_score /= #10000 const

scoreboard players operation #vl.y main_score *= @s phys_veh.offset2.x
scoreboard players operation #temp main_score = #vl.z main_score
scoreboard players operation #temp main_score *= @s phys_veh.offset2.z
scoreboard players operation #vl.y main_score -= #temp main_score
scoreboard players operation #vl.y main_score /= #10000 const

scoreboard players operation #vl.z main_score *= @s phys_veh.offset2.y
scoreboard players operation #temp main_score = #main.rotated_spin.y main_score
scoreboard players operation #temp main_score *= @s phys_veh.offset2.x
scoreboard players operation #vl.z main_score -= #temp main_score
scoreboard players operation #vl.z main_score /= #10000 const

scoreboard players operation #vf.x main_score = #vl.x main_score
scoreboard players operation #vf.y main_score = #vl.y main_score
scoreboard players operation #vf.z main_score = #vl.z main_score

scoreboard players operation #vl.x main_score += #main.velocity.x main_score
scoreboard players operation #vl.y main_score += #main.velocity.y main_score
scoreboard players operation #vl.z main_score += #main.velocity.z main_score

scoreboard players operation #vf.x main_score += #main.last_velocity.x main_score
scoreboard players operation #vf.y main_score += #main.last_velocity.y main_score
scoreboard players operation #vf.z main_score += #main.last_velocity.z main_score


function physical_vehicles:zzz/vehicle/at_parts/wheel/unit_vector/vl
function physical_vehicles:zzz/vehicle/at_parts/wheel/unit_vector/vf

scoreboard players operation #vl.x main_score += #vl.y main_score
scoreboard players operation #vl.x main_score += #vl.z main_score

scoreboard players operation #vf.x main_score += #vf.y main_score
scoreboard players operation #vf.x main_score += #vf.z main_score

scoreboard players operation #kappa main_score = @s phys_veh.radius
scoreboard players operation #kappa main_score *= @s phys_veh.spin.x
scoreboard players operation #kappa main_score /= #10000 const

# fp = 1 - k_p * (p - p_0)
scoreboard players operation #B main_score = @s phys_veh.wheel.pressure
scoreboard players operation #B main_score -= @s phys_veh.wheel.pressure0
scoreboard players operation #B main_score *= @s phys_veh.wheel.pressure_coeff
scoreboard players operation #B main_score /= #-100 const
scoreboard players add #B main_score 100

# D = mu * F_z

scoreboard players operation #D main_score = @s phys_veh.coeff_friction
scoreboard players operation #D main_score *= #B main_score
scoreboard players operation #D main_score /= #100 const
scoreboard players operation #DS main_score = #D main_score
scoreboard players operation #Dc main_score = #D main_score
scoreboard players operation #Dc main_score /= #10 const
scoreboard players operation #D main_score *= @s phys_veh.wheel.suspension.force
scoreboard players operation #D main_score /= #1000 const
scoreboard players operation #DO main_score = #D main_score

scoreboard players operation #DL main_score = #D main_score
scoreboard players operation #DM main_score = #D main_score
scoreboard players operation #D main_score *= @s phys_veh.wheel.longitudinal.peak_factor
scoreboard players operation #DL main_score *= @s phys_veh.wheel.lateral.peak_factor
scoreboard players operation #D main_score /= #100 const
scoreboard players operation #DL main_score /= #100 const

# B = k_B * (F_z / F_z0) * fp
scoreboard players operation #BL main_score = #B main_score
scoreboard players operation #BM main_score = #B main_score
scoreboard players operation #B main_score *= @s phys_veh.wheel.longitudinal.stiffness
scoreboard players operation #BL main_score *= @s phys_veh.wheel.lateral.stiffness
scoreboard players operation #B main_score /= #100 const
scoreboard players operation #BL main_score /= #100 const

# t_approx = T / T_0
#scoreboard players operation #t_approx main_score = @s phys_veh.wheel.temperature
#scoreboard players operation #t_approx main_score *= #100 const
#scoreboard players operation #t_approx main_score /= @s phys_veh.wheel.temperature0

# E = k_E * (1 / (1 + t_approx + t_approx**2 / 2))
#scoreboard players set #E main_score 30000
#scoreboard players operation #temp main_score = #t_approx main_score
#scoreboard players operation #temp main_score *= #t_approx main_score
#scoreboard players operation #temp main_score /= #200 const
#scoreboard players operation #temp main_score += #t_approx main_score
#scoreboard players add #temp main_score 100
#scoreboard players operation #E main_score /= #temp main_score
#scoreboard players operation #EL main_score = #E main_score
#scoreboard players operation #EM main_score = #E main_score
#scoreboard players operation #E main_score *= @s phys_veh.wheel.longitudinal.curvature
#scoreboard players operation #E main_score /= #100 const
#scoreboard players operation #EL main_score *= @s phys_veh.wheel.lateral.curvature
#scoreboard players operation #EL main_score /= #100 const


execute if entity @s[tag=phys_veh.wheel.steering] run function physical_vehicles:zzz/unit_tests/aligning_torque
scoreboard players operation #IN:arctan2.X main_score = #vf.x main_score
scoreboard players operation #IN:arctan2.X main_score -= #kappa main_score
scoreboard players operation #IN:arctan2.Y main_score = #vl.x main_score
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
function midwut:help/math/trig_func/arc/tan2
execute if score #OUT:arctan2 main_score matches 180001.. run scoreboard players remove #OUT:arctan2 main_score 360000
scoreboard players operation #OUT:arctan2 main_score *= #BL main_score
scoreboard players operation #OUT:arctan2 main_score /= #1000 const
scoreboard players operation #OUT:arctan2 main_score *= #pi_3 const
scoreboard players operation #OUT:arctan2 main_score /= #1800 const
scoreboard players operation #alpha main_score = #OUT:arctan2 main_score
scoreboard players operation #IN:tan main_score = #alpha main_score
function midwut:help/math/trig_func/arc/tan
execute if score #OUT:arctan main_score matches 180001.. run scoreboard players remove #OUT:arctan main_score 360000
scoreboard players operation #OUT:arctan main_score /= #10 const
scoreboard players operation #OUT:arctan main_score *= #pi_4 const
scoreboard players operation #OUT:arctan main_score /= #18000 const
scoreboard players operation #temp main_score = #alpha main_score
scoreboard players operation #temp main_score -= #OUT:arctan main_score
scoreboard players operation #temp main_score /= #10 const
scoreboard players operation #temp main_score *= @s phys_veh.wheel.lateral.curvature
scoreboard players operation #temp main_score /= #10 const
scoreboard players operation #alpha main_score -= #temp main_score
scoreboard players operation #IN:tan main_score = #alpha main_score
function midwut:help/math/trig_func/arc/tan
execute if score #OUT:arctan main_score matches 180001.. run scoreboard players remove #OUT:arctan main_score 360000
scoreboard players operation #OUT:arctan main_score *= @s phys_veh.wheel.lateral.shape_factor
scoreboard players operation #OUT:arctan main_score /= #100 const
execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #OUT:arctan main_score %= #360000 const
function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
# scoreboard players operation #OUT:sin main_score *= #fp main_score
# scoreboard players operation #OUT:sin main_score /= #10000 const
scoreboard players operation #OUT:sin main_score /= #10 const

scoreboard players operation #fx2 main_score = #OUT:sin main_score
scoreboard players operation #fx2 main_score *= #Dc main_score
scoreboard players operation #fx2 main_score /= #100 const

scoreboard players operation #OUT:sin main_score *= #DL main_score
scoreboard players operation #OUT:sin main_score /= #100 const
scoreboard players operation #fx main_score = #OUT:sin main_score
#execute if entity @s[tag=phys_veh.wheel.driving] run tellraw @a [{"score":{"name":"#fx","objective":"main_score"}}]


scoreboard players operation #kappa main_score -= #vf.x main_score
scoreboard players operation #vf main_score = #kappa main_score
scoreboard players operation #temp main_score = #vf.x main_score
execute if score #temp main_score matches ..-1 run scoreboard players operation #temp main_score *= #-1 const
scoreboard players operation #kappa main_score *= #1000 const
scoreboard players operation #kappa main_score /= #temp main_score
#tellraw @a [{"score":{name:"#kappa",objective:"main_score"}}]
scoreboard players operation #kappa main_score *= #B main_score
scoreboard players operation #kappa main_score /= #10 const
scoreboard players operation #IN:tan main_score = #kappa main_score
function midwut:help/math/trig_func/arc/tan
execute if score #OUT:arctan main_score matches 180001.. run scoreboard players remove #OUT:arctan main_score 360000
scoreboard players operation #OUT:arctan main_score /= #10 const
scoreboard players operation #OUT:arctan main_score *= #pi_4 const
scoreboard players operation #OUT:arctan main_score /= #18000 const
scoreboard players operation #temp main_score = #kappa main_score
scoreboard players operation #temp main_score -= #OUT:arctan main_score
scoreboard players operation #temp main_score /= #10 const
scoreboard players operation #temp main_score *= @s phys_veh.wheel.longitudinal.curvature
scoreboard players operation #temp main_score /= #10 const
scoreboard players operation #kappa main_score -= #temp main_score
scoreboard players operation #IN:tan main_score = #kappa main_score
function midwut:help/math/trig_func/arc/tan
execute if score #OUT:arctan main_score matches 180001.. run scoreboard players remove #OUT:arctan main_score 360000
scoreboard players operation #OUT:arctan main_score *= @s phys_veh.wheel.longitudinal.shape_factor
scoreboard players operation #OUT:arctan main_score /= #100 const
execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #OUT:arctan main_score %= #360000 const
function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
scoreboard players operation #OUT:sin main_score /= #10 const

scoreboard players operation #fy2 main_score = #OUT:sin main_score
scoreboard players operation #fy2 main_score *= #Dc main_score
scoreboard players operation #fy2 main_score /= #100 const

scoreboard players operation #OUT:sin main_score *= #D main_score
scoreboard players operation #OUT:sin main_score /= #100 const
scoreboard players operation #fy main_score = #OUT:sin main_score

#scoreboard players operation #fy2 main_score *= #100 const
#scoreboard players operation #fy2 main_score /= @s phys_veh.wheel.suspension.force
#execute if entity @s[tag=phys_veh.wheel.driving] run tellraw @a [{"score":{name:"#fy2",objective:"main_score"}}]



scoreboard players operation #IN:arctan2.X main_score = #vf main_score
scoreboard players operation #IN:arctan2.Y main_score = #vl.x main_score
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1
function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #1000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score


scoreboard players operation #smoke main_score = #IN:arctan2.X main_score
scoreboard players operation #smoke main_score *= #DS main_score
scoreboard players operation #smoke main_score *= @s phys_veh.wheel.smoke_factor
scoreboard players operation #smoke main_score /= #700 const
#execute if score #smoke main_score matches 10000.. positioned as @s run function physical_vehicles:zzz/vehicle/at_parts/wheel/smoke/0

#execute if score #IN:arctan2.X main_score matches 400.. run function physical_vehicles:zzz/vehicle/at_parts/wheel/sounds/pre
#scoreboard players operation #temperature main_score *= #10 const
#scoreboard players operation #temp main_score = @s phys_veh.mass
#scoreboard players operation #temp main_score *= @s phys_veh.wheel.heat_capacity
#scoreboard players operation #temperature main_score /= #temp main_score
#scoreboard players operation @s phys_veh.wheel.temperature += #temperature main_score


scoreboard players operation #IN:arctan2.X main_score = #fy main_score
scoreboard players operation #IN:arctan2.Y main_score = #fx main_score
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score /= #10 const
scoreboard players operation #midwut.temp main_score /= #10 const
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1
function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #OUT:cos main_score /= #10 const
scoreboard players operation #IN:arctan2.X main_score *= #100 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

#scoreboard players operation #D main_score *= #fp main_score
#scoreboard players operation #D main_score /= #100 const

# Fy0*   sqrt(1-(Fx/Fx0)^2)
# or
#    sqrt(1/(1+(ПРОД/БОК)^2))
#scoreboard players operation #D main_score *= #10 const
execute if score #IN:arctan2.X main_score > #DO main_score run function physical_vehicles:zzz/vehicle/at_parts/wheel/normalize
execute if score #vf main_score matches -350..350 unless score #vf main_score matches 0 run function physical_vehicles:zzz/vehicle/at_parts/wheel/decrease_fy
execute if score #vl.x main_score matches -350..350 unless score #vl.x main_score matches 0 run function physical_vehicles:zzz/vehicle/at_parts/wheel/decrease_fx
execute if score #vf.x main_score matches -100..100 unless score #vf.x main_score matches 0 run function physical_vehicles:zzz/vehicle/at_parts/wheel/decrease_fz
#scoreboard players operation #temp main_score = #vl.x main_score
#scoreboard players operation #temp main_score *= #1000 const
#scoreboard players operation #temp main_score /= #phys_veh.velocity_threshold const
#execute if score #temp main_score matches -999..999 unless score #temp main_score matches -10..10 run function physical_vehicles:zzz/vehicle/at_parts/wheel/decrease_fx
#scoreboard players operation #temp main_score = #vf main_score
#scoreboard players operation #temp main_score *= #1000 const
#scoreboard players operation #temp main_score /= #phys_veh.velocity_threshold const
#execute if score #temp main_score matches -999..999 unless score #temp main_score matches -10..10 run function physical_vehicles:zzz/vehicle/at_parts/wheel/decrease_fy



scoreboard players operation #fy_w main_score = #fy main_score
scoreboard players operation #fy_w main_score /= @s phys_veh.mass
scoreboard players operation #fy main_score /= #main.mass main_score
scoreboard players operation #fx main_score /= #main.mass main_score
function physical_vehicles:zzz/vehicle/at_parts/wheel/unit_vector/fx
function physical_vehicles:zzz/vehicle/at_parts/wheel/unit_vector/fy

scoreboard players operation #x2 main_score += #x3 main_score
scoreboard players operation #y2 main_score += #y3 main_score
scoreboard players operation #z2 main_score += #z3 main_score
scoreboard players operation #dv.x.x main_score += #x2 main_score
scoreboard players operation #dv.x.y main_score += #y2 main_score
scoreboard players operation #dv.x.z main_score += #z2 main_score
execute if entity @s[tag=phys_veh.wheel.steering] run scoreboard players operation #dw.y.x main_score += #fz main_score


scoreboard players operation #temp main_score = @s phys_veh.radius
scoreboard players operation #temp main_score /= #10 const
scoreboard players operation #temp main_score *= #fy_w main_score
scoreboard players operation #temp main_score /= #tick_rate main_score
scoreboard players operation #temp main_score /= @s phys_veh.moment_of_inertia_no_mass.x
scoreboard players operation @s phys_veh.spin.x -= #temp main_score
#scoreboard players operation #temp main_score /= #10 const



# scoreboard players operation #x2 main_score /= #10 const
# scoreboard players operation #y2 main_score /= #10 const
# scoreboard players operation #z2 main_score /= #10 const

scoreboard players operation #x3 main_score = @s phys_veh.offset2.y
scoreboard players operation #x3 main_score *= #z2 main_score
scoreboard players operation #temp main_score = @s phys_veh.offset2.z
scoreboard players operation #temp main_score *= #y2 main_score
scoreboard players operation #x3 main_score -= #temp main_score
scoreboard players operation #x3 main_score /= #1000 const

scoreboard players operation #y3 main_score = @s phys_veh.offset2.x
scoreboard players operation #y3 main_score *= #z2 main_score
scoreboard players operation #temp main_score = @s phys_veh.offset2.z
scoreboard players operation #temp main_score *= #x2 main_score
scoreboard players operation #y3 main_score -= #temp main_score
scoreboard players operation #y3 main_score /= #1000 const

scoreboard players operation #z3 main_score = @s phys_veh.offset2.x
scoreboard players operation #z3 main_score *= #y2 main_score
scoreboard players operation #temp main_score = @s phys_veh.offset2.y
scoreboard players operation #temp main_score *= #x2 main_score
scoreboard players operation #z3 main_score -= #temp main_score
scoreboard players operation #z3 main_score /= #1000 const


scoreboard players operation #dw.x.x main_score += #x3 main_score
scoreboard players operation #dw.x.y main_score -= #y3 main_score
scoreboard players operation #dw.x.z main_score += #z3 main_score

execute if score @s phys_veh.wheel.suspension.force matches 1.. run function physical_vehicles:zzz/vehicle/at_parts/wheel/apply_force

