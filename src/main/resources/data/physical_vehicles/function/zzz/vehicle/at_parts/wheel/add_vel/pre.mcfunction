tag @s add phys_veh.on_ground

execute if score #count_wheels.total main_score matches ..1 run function physical_vehicles:zzz/vehicle/at_parts/wheel/add_vel/norm/0
execute if score #count_wheels.total main_score matches 2 run function physical_vehicles:zzz/vehicle/at_parts/wheel/add_vel/norm/1
execute if score #count_wheels.total main_score matches 3.. run function physical_vehicles:zzz/vehicle/at_parts/wheel/add_vel/norm/2

scoreboard players operation #left.z main_score = #norm.x main_score
scoreboard players operation #left.x main_score = #norm.y main_score
scoreboard players operation #left.y main_score = #norm.z main_score

scoreboard players operation #left.x main_score *= #phys_veh.unit_vector.3.001.z main_score
scoreboard players operation #temp main_score = #left.y main_score
scoreboard players operation #temp main_score *= #phys_veh.unit_vector.3.001.y main_score
scoreboard players operation #left.x main_score -= #temp main_score
scoreboard players operation #left.x main_score /= #1000 const

scoreboard players operation #left.y main_score *= #phys_veh.unit_vector.3.001.x main_score
scoreboard players operation #temp main_score = #left.z main_score
scoreboard players operation #temp main_score *= #phys_veh.unit_vector.3.001.z main_score
scoreboard players operation #left.y main_score -= #temp main_score
scoreboard players operation #left.y main_score /= #1000 const

scoreboard players operation #left.z main_score *= #phys_veh.unit_vector.3.001.y main_score
scoreboard players operation #temp main_score = #norm.y main_score
scoreboard players operation #temp main_score *= #phys_veh.unit_vector.3.001.x main_score
scoreboard players operation #left.z main_score -= #temp main_score
scoreboard players operation #left.z main_score /= #1000 const

scoreboard players operation #IN:arctan2.X main_score = #left.z main_score
scoreboard players operation #IN:arctan2.Y main_score = #left.x main_score
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #IN:arctan2.Y main_score = #left.y main_score
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score


scoreboard players operation #left.x main_score *= #1000 const
scoreboard players operation #left.y main_score *= #1000 const
scoreboard players operation #left.z main_score *= #1000 const
scoreboard players operation #left.x main_score /= #IN:arctan2.X main_score
scoreboard players operation #left.y main_score /= #IN:arctan2.X main_score
scoreboard players operation #left.z main_score /= #IN:arctan2.X main_score




scoreboard players operation #forward.z main_score = #left.x main_score
scoreboard players operation #forward.x main_score = #left.y main_score
scoreboard players operation #forward.y main_score = #left.z main_score

scoreboard players operation #forward.x main_score *= #norm.z main_score
scoreboard players operation #temp main_score = #forward.y main_score
scoreboard players operation #temp main_score *= #norm.y main_score
scoreboard players operation #forward.x main_score -= #temp main_score
scoreboard players operation #forward.x main_score /= #1000 const

scoreboard players operation #forward.y main_score *= #norm.x main_score
scoreboard players operation #temp main_score = #forward.z main_score
scoreboard players operation #temp main_score *= #norm.z main_score
scoreboard players operation #forward.y main_score -= #temp main_score
scoreboard players operation #forward.y main_score /= #1000 const

scoreboard players operation #forward.z main_score *= #norm.y main_score
scoreboard players operation #temp main_score = #left.y main_score
scoreboard players operation #temp main_score *= #norm.x main_score
scoreboard players operation #forward.z main_score -= #temp main_score
scoreboard players operation #forward.z main_score /= #1000 const

scoreboard players operation #IN:arctan2.X main_score = #forward.z main_score
scoreboard players operation #IN:arctan2.Y main_score = #forward.x main_score
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #IN:arctan2.Y main_score = #forward.y main_score
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score


scoreboard players operation #forward.x main_score *= #1000 const
scoreboard players operation #forward.y main_score *= #1000 const
scoreboard players operation #forward.z main_score *= #1000 const
scoreboard players operation #forward.x main_score /= #IN:arctan2.X main_score
scoreboard players operation #forward.y main_score /= #IN:arctan2.X main_score
scoreboard players operation #forward.z main_score /= #IN:arctan2.X main_score







scoreboard players operation #left_f.z main_score = #norm.x main_score
scoreboard players operation #left_f.x main_score = #norm.y main_score
scoreboard players operation #left_f.y main_score = #norm.z main_score

scoreboard players operation #left_f.x main_score *= #phys_veh.unit_vector2.3.001.z main_score
scoreboard players operation #temp main_score = #left_f.y main_score
scoreboard players operation #temp main_score *= #phys_veh.unit_vector2.3.001.y main_score
scoreboard players operation #left_f.x main_score -= #temp main_score
scoreboard players operation #left_f.x main_score /= #1000 const

scoreboard players operation #left_f.y main_score *= #phys_veh.unit_vector2.3.001.x main_score
scoreboard players operation #temp main_score = #left_f.z main_score
scoreboard players operation #temp main_score *= #phys_veh.unit_vector2.3.001.z main_score
scoreboard players operation #left_f.y main_score -= #temp main_score
scoreboard players operation #left_f.y main_score /= #1000 const

scoreboard players operation #left_f.z main_score *= #phys_veh.unit_vector2.3.001.y main_score
scoreboard players operation #temp main_score = #norm.y main_score
scoreboard players operation #temp main_score *= #phys_veh.unit_vector2.3.001.x main_score
scoreboard players operation #left_f.z main_score -= #temp main_score
scoreboard players operation #left_f.z main_score /= #1000 const

scoreboard players operation #IN:arctan2.X main_score = #left_f.z main_score
scoreboard players operation #IN:arctan2.Y main_score = #left_f.x main_score
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #IN:arctan2.Y main_score = #left_f.y main_score
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score


scoreboard players operation #left_f.x main_score *= #1000 const
scoreboard players operation #left_f.y main_score *= #1000 const
scoreboard players operation #left_f.z main_score *= #1000 const
scoreboard players operation #left_f.x main_score /= #IN:arctan2.X main_score
scoreboard players operation #left_f.y main_score /= #IN:arctan2.X main_score
scoreboard players operation #left_f.z main_score /= #IN:arctan2.X main_score




scoreboard players operation #forward_f.z main_score = #left_f.x main_score
scoreboard players operation #forward_f.x main_score = #left_f.y main_score
scoreboard players operation #forward_f.y main_score = #left_f.z main_score

scoreboard players operation #forward_f.x main_score *= #norm.z main_score
scoreboard players operation #temp main_score = #forward_f.y main_score
scoreboard players operation #temp main_score *= #norm.y main_score
scoreboard players operation #forward_f.x main_score -= #temp main_score
scoreboard players operation #forward_f.x main_score /= #1000 const

scoreboard players operation #forward_f.y main_score *= #norm.x main_score
scoreboard players operation #temp main_score = #forward_f.z main_score
scoreboard players operation #temp main_score *= #norm.z main_score
scoreboard players operation #forward_f.y main_score -= #temp main_score
scoreboard players operation #forward_f.y main_score /= #1000 const

scoreboard players operation #forward_f.z main_score *= #norm.y main_score
scoreboard players operation #temp main_score = #left_f.y main_score
scoreboard players operation #temp main_score *= #norm.x main_score
scoreboard players operation #forward_f.z main_score -= #temp main_score
scoreboard players operation #forward_f.z main_score /= #1000 const

scoreboard players operation #IN:arctan2.X main_score = #forward_f.z main_score
scoreboard players operation #IN:arctan2.Y main_score = #forward_f.x main_score
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #IN:arctan2.Y main_score = #forward_f.y main_score
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score


scoreboard players operation #forward_f.x main_score *= #1000 const
scoreboard players operation #forward_f.y main_score *= #1000 const
scoreboard players operation #forward_f.z main_score *= #1000 const
scoreboard players operation #forward_f.x main_score /= #IN:arctan2.X main_score
scoreboard players operation #forward_f.y main_score /= #IN:arctan2.X main_score
scoreboard players operation #forward_f.z main_score /= #IN:arctan2.X main_score



scoreboard players set #count_driving_wheels main_score 0

execute as @e[type=#physical_vehicles:refers_to_vehicle/wheel,tag=physical_vehicle,tag=phys_veh.wheel,tag=phys_veh.selected_part,tag=phys_veh.wheel.on_ground,distance=..96,scores={phys_veh.wheel.suspension.force=1..}] run function physical_vehicles:zzz/vehicle/at_parts/wheel/suspension_force


scoreboard players operation #main.last_velocity.x main_score = @s phys_veh.velocity.x
scoreboard players operation #main.last_velocity.y main_score = @s phys_veh.velocity.y
scoreboard players operation #main.last_velocity.z main_score = @s phys_veh.velocity.z

scoreboard players operation #dv.x.x main_score /= #tick_rate_0 main_score
scoreboard players operation #dv.x.y main_score /= #tick_rate_0 main_score
scoreboard players operation #dv.x.z main_score /= #tick_rate_0 main_score
scoreboard players operation @s phys_veh.velocity.x += #dv.x.x main_score
scoreboard players operation @s phys_veh.velocity.y += #dv.x.y main_score
scoreboard players operation @s phys_veh.velocity.z += #dv.x.z main_score
scoreboard players set #dv.x.x main_score 0
scoreboard players set #dv.x.y main_score 0
scoreboard players set #dv.x.z main_score 0
scoreboard players operation #main.velocity.x main_score = @s phys_veh.velocity.x
scoreboard players operation #main.velocity.y main_score = @s phys_veh.velocity.y
scoreboard players operation #main.velocity.z main_score = @s phys_veh.velocity.z

function physical_vehicles:zzz/vehicle/math/spin/rot

execute as @e[type=#physical_vehicles:refers_to_vehicle/wheel,tag=physical_vehicle,tag=phys_veh.wheel,tag=phys_veh.selected_part,tag=phys_veh.wheel.on_ground,distance=..96] run function physical_vehicles:zzz/vehicle/at_parts/wheel/on_ground_calcs


function physical_vehicles:zzz/vehicle/at_parts/wheel/add_vel/0
