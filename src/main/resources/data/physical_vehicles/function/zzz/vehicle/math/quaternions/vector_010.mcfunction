#> physical_vehicles:zzz/vehicle/math/quaternions/vector_010

execute store result storage midwut:main Data.rotation.010_x float 0.0001 run scoreboard players get #m01 main_score
execute store result storage midwut:main Data.rotation.010_y float 0.0001 run scoreboard players get #m11 main_score
execute store result storage midwut:main Data.rotation.010_z float 0.0001 run scoreboard players get #m21 main_score

#scoreboard players operation #phys_veh.unit_vector.3.010.x main_score = #m01_3 main_score
#scoreboard players operation #phys_veh.unit_vector.3.010.y main_score = #m11_3 main_score
#scoreboard players operation #phys_veh.unit_vector.3.010.z main_score = #m21_3 main_score

#scoreboard players operation #IN:arctan2.X main_score = #phys_veh.unit_vector.3.010.z main_score
#scoreboard players operation #IN:arctan2.Y main_score = #phys_veh.unit_vector.3.010.x main_score
##execute store result storage midwut:main Data.rotation.010_y float 0.001 store result score #phys_veh.rotation.010.y main_score run function midwut:help/math/trig_func/arc/tan2
#execute store result storage midwut:main Data.rotation.010_y float 0.001 run function midwut:help/math/trig_func/arc/tan2

#scoreboard players operation #IN:arctan2.X main_score = #phys_veh.unit_vector.3.010.z main_score
#scoreboard players operation #IN:arctan2.Y main_score = #phys_veh.unit_vector.3.010.x main_score
#execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
#execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
#execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
#scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
#scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
#execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

#function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
#scoreboard players operation #IN:arctan2.X main_score *= #10000 const
#scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

#scoreboard players operation #IN:arctan2.Y main_score = #phys_veh.unit_vector.3.010.y main_score
##execute store result storage midwut:main Data.rotation.010_x float 0.001 store result score #phys_veh.rotation.010.x main_score run function midwut:help/math/trig_func/arc/tan2
#execute store result storage midwut:main Data.rotation.010_x float 0.001 run function midwut:help/math/trig_func/arc/tan2
