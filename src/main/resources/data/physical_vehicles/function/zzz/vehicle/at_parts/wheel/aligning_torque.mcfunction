execute if score #vf.x main_score matches -10..10 if score #vl.x main_score matches -10..10 run return fail

scoreboard players operation #BM main_score *= @s phys_veh.wheel.aligning_torque.stiffness
scoreboard players operation #BM main_score /= #100 const
scoreboard players operation #EM main_score *= @s phys_veh.wheel.aligning_torque.curvature
scoreboard players operation #EM main_score /= #100 const
scoreboard players operation #DM main_score *= @s phys_veh.wheel.aligning_torque.peak_factor
scoreboard players operation #DM main_score /= #100 const

scoreboard players operation #IN:arctan2.X main_score = #vf.x main_score
#scoreboard players operation #IN:arctan2.X main_score -= #kappa main_score
scoreboard players operation #IN:arctan2.Y main_score = #vl.x main_score
#execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
function midwut:help/math/trig_func/arc/tan2

execute if score #OUT:arctan2 main_score matches 180001.. run scoreboard players remove #OUT:arctan2 main_score 360000

scoreboard players operation #alpha2 main_score = #OUT:arctan2 main_score
scoreboard players operation #alpha2 main_score *= #BM main_score
scoreboard players operation #alpha2 main_score /= #10000 const
scoreboard players operation #alpha2 main_score *= #pi_4 const
scoreboard players operation #alpha2 main_score /= #1800 const
scoreboard players operation #IN:tan main_score = #alpha2 main_score
function midwut:help/math/trig_func/arc/tan
execute if score #OUT:arctan main_score matches 180001.. run scoreboard players remove #OUT:arctan main_score 360000
scoreboard players operation #OUT:arctan main_score /= #10 const
scoreboard players operation #OUT:arctan main_score *= #pi_4 const
scoreboard players operation #OUT:arctan main_score /= #18000 const
scoreboard players operation #temp main_score = #alpha2 main_score
scoreboard players operation #temp main_score -= #OUT:arctan main_score
scoreboard players operation #temp main_score /= #100 const
scoreboard players operation #temp main_score *= @s phys_veh.wheel.aligning_torque.curvature
scoreboard players operation #IN:tan main_score -= #temp main_score
function midwut:help/math/trig_func/arc/tan
execute if score #OUT:arctan main_score matches 180001.. run scoreboard players remove #OUT:arctan main_score 360000
scoreboard players operation #OUT:arctan main_score *= @s phys_veh.wheel.aligning_torque.shape_factor
scoreboard players operation #OUT:arctan main_score /= #100 const
execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #OUT:arctan main_score %= #360000 const
function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
# scoreboard players operation #OUT:sin main_score *= #fp main_score
# scoreboard players operation #OUT:sin main_score /= #10000 const
#scoreboard players operation #OUT:sin main_score /= #10 const
scoreboard players operation #OUT:sin main_score *= #DM main_score
scoreboard players operation #OUT:sin main_score /= #10000 const
scoreboard players operation #fz main_score = #OUT:sin main_score
#scoreboard players operation #dw.y.x main_score += #OUT:sin main_score
scoreboard players add #count_driving_wheels main_score 1