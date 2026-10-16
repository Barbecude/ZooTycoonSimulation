#> physical_vehicles:zzz/summon_data/zzzzzz_data/data/item_display
execute if entity @s[tag=!phys_veh.disable.rotation] run data modify entity @s transformation.left_rotation set from storage midwut:main Data.data.transformation.main_rotation
execute store result score @s phys_veh.offset.x run data get storage midwut:main Data2.data.phys_veh.offset[0] 1000
execute store result score @s phys_veh.offset.y run data get storage midwut:main Data2.data.phys_veh.offset[1] 1000
execute store result score @s phys_veh.offset.z run data get storage midwut:main Data2.data.phys_veh.offset[2] 1000
execute if data storage midwut:main Data2.data.phys_veh.mass store result score @s phys_veh.mass run data get storage midwut:main Data2.data.phys_veh.mass 1
execute if data storage midwut:main Data2.data.phys_veh.object_id store result score @s phys_veh.id.object run data get storage midwut:main Data2.data.phys_veh.object_id
execute if data storage midwut:main Data2.data.phys_veh.object_parent_id store result score @s phys_veh.id.object_parent run data get storage midwut:main Data2.data.phys_veh.object_parent_id
execute if data storage midwut:main Data2.data.phys_veh.order_id store result score @s phys_veh.id.order run data get storage midwut:main Data2.data.phys_veh.order_id
execute if data storage midwut:main Data2.data.phys_veh.input_id store result score @s phys_veh.id.input run data get storage midwut:main Data2.data.phys_veh.input_id
execute if score @s phys_veh.id.order = @s phys_veh.id.order run tag @s add phys_veh.custom_order

execute unless predicate midwut:check/vehicle/any unless score @s phys_veh.id.object_parent = @s phys_veh.id.object_parent run scoreboard players operation @s phys_veh.offset.x -= #mass_center.x main_score
execute unless predicate midwut:check/vehicle/any unless score @s phys_veh.id.object_parent = @s phys_veh.id.object_parent run scoreboard players operation @s phys_veh.offset.y -= #mass_center.y main_score
execute unless predicate midwut:check/vehicle/any unless score @s phys_veh.id.object_parent = @s phys_veh.id.object_parent run scoreboard players operation @s phys_veh.offset.z -= #mass_center.z main_score

execute if data storage midwut:main Data2.data.phys_veh.y_angle_speed store result score @s phys_veh.spin.y run data get storage midwut:main Data2.data.phys_veh.y_angle_speed 8.72664626
execute if data storage midwut:main Data2.data.phys_veh.x_angle_speed store result score @s phys_veh.spin.x run data get storage midwut:main Data2.data.phys_veh.x_angle_speed 8.72664626
execute if data storage midwut:main Data2.data.phys_veh.z_angle_speed store result score @s phys_veh.spin.z run data get storage midwut:main Data2.data.phys_veh.z_angle_speed 8.72664626
execute if data storage midwut:main Data2.data.phys_veh.y_angle_positive_limit store result score @s phys_veh.add_spin.y run data get storage midwut:main Data2.data.phys_veh.y_angle_positive_limit 174.6
execute if data storage midwut:main Data2.data.phys_veh.x_angle_positive_limit store result score @s phys_veh.add_spin.x run data get storage midwut:main Data2.data.phys_veh.x_angle_positive_limit 174.6
execute if data storage midwut:main Data2.data.phys_veh.z_angle_positive_limit store result score @s phys_veh.add_spin.z run data get storage midwut:main Data2.data.phys_veh.z_angle_positive_limit 174.6
execute if data storage midwut:main Data2.data.phys_veh.y_angle_negative_limit store result score @s phys_veh.coll_spin.y run data get storage midwut:main Data2.data.phys_veh.y_angle_negative_limit 174.6
execute if data storage midwut:main Data2.data.phys_veh.x_angle_negative_limit store result score @s phys_veh.coll_spin.x run data get storage midwut:main Data2.data.phys_veh.x_angle_negative_limit 174.6
execute if data storage midwut:main Data2.data.phys_veh.z_angle_negative_limit store result score @s phys_veh.coll_spin.z run data get storage midwut:main Data2.data.phys_veh.z_angle_negative_limit 174.6
execute if data storage midwut:main Data2.data.phys_veh.x_traverse_sound run tag @s add phys_veh.add_angle.x.have_sound
execute if data storage midwut:main Data2.data.phys_veh.y_traverse_sound run tag @s add phys_veh.add_angle.y.have_sound
execute if data storage midwut:main Data2.data.phys_veh.z_traverse_sound run tag @s add phys_veh.add_angle.z.have_sound
execute if data storage midwut:main Data2.data.phys_veh{y_angle_positive_control:"w"} run tag @s add phys_veh.add_angle.y.positive_w
execute if data storage midwut:main Data2.data.phys_veh{y_angle_positive_control:"a"} run tag @s add phys_veh.add_angle.y.positive_a
execute if data storage midwut:main Data2.data.phys_veh{y_angle_positive_control:"s"} run tag @s add phys_veh.add_angle.y.positive_s
execute if data storage midwut:main Data2.data.phys_veh{y_angle_positive_control:"d"} run tag @s add phys_veh.add_angle.y.positive_d
execute if data storage midwut:main Data2.data.phys_veh{y_angle_positive_control:"sprint"} run tag @s add phys_veh.add_angle.y.positive_sprint
execute if data storage midwut:main Data2.data.phys_veh{y_angle_positive_control:"jump"} run tag @s add phys_veh.add_angle.y.positive_jump
execute if data storage midwut:main Data2.data.phys_veh{y_angle_positive_control:"use"} run tag @s add phys_veh.add_angle.y.positive_use
execute if data storage midwut:main Data2.data.phys_veh{x_angle_positive_control:"w"} run tag @s add phys_veh.add_angle.x.positive_w
execute if data storage midwut:main Data2.data.phys_veh{x_angle_positive_control:"a"} run tag @s add phys_veh.add_angle.x.positive_a
execute if data storage midwut:main Data2.data.phys_veh{x_angle_positive_control:"s"} run tag @s add phys_veh.add_angle.x.positive_s
execute if data storage midwut:main Data2.data.phys_veh{x_angle_positive_control:"d"} run tag @s add phys_veh.add_angle.x.positive_d
execute if data storage midwut:main Data2.data.phys_veh{x_angle_positive_control:"sprint"} run tag @s add phys_veh.add_angle.x.positive_sprint
execute if data storage midwut:main Data2.data.phys_veh{x_angle_positive_control:"jump"} run tag @s add phys_veh.add_angle.x.positive_jump
execute if data storage midwut:main Data2.data.phys_veh{x_angle_positive_control:"use"} run tag @s add phys_veh.add_angle.x.positive_use
execute if data storage midwut:main Data2.data.phys_veh{z_angle_positive_control:"w"} run tag @s add phys_veh.add_angle.z.positive_w
execute if data storage midwut:main Data2.data.phys_veh{z_angle_positive_control:"a"} run tag @s add phys_veh.add_angle.z.positive_a
execute if data storage midwut:main Data2.data.phys_veh{z_angle_positive_control:"s"} run tag @s add phys_veh.add_angle.z.positive_s
execute if data storage midwut:main Data2.data.phys_veh{z_angle_positive_control:"d"} run tag @s add phys_veh.add_angle.z.positive_d
execute if data storage midwut:main Data2.data.phys_veh{z_angle_positive_control:"sprint"} run tag @s add phys_veh.add_angle.z.positive_sprint
execute if data storage midwut:main Data2.data.phys_veh{z_angle_positive_control:"jump"} run tag @s add phys_veh.add_angle.z.positive_jump
execute if data storage midwut:main Data2.data.phys_veh{z_angle_positive_control:"use"} run tag @s add phys_veh.add_angle.z.positive_use
execute if data storage midwut:main Data2.data.phys_veh{y_angle_negative_control:"w"} run tag @s add phys_veh.add_angle.y.negative_w
execute if data storage midwut:main Data2.data.phys_veh{y_angle_negative_control:"a"} run tag @s add phys_veh.add_angle.y.negative_a
execute if data storage midwut:main Data2.data.phys_veh{y_angle_negative_control:"s"} run tag @s add phys_veh.add_angle.y.negative_s
execute if data storage midwut:main Data2.data.phys_veh{y_angle_negative_control:"d"} run tag @s add phys_veh.add_angle.y.negative_d
execute if data storage midwut:main Data2.data.phys_veh{y_angle_negative_control:"sprint"} run tag @s add phys_veh.add_angle.y.negative_sprint
execute if data storage midwut:main Data2.data.phys_veh{y_angle_negative_control:"jump"} run tag @s add phys_veh.add_angle.y.negative_jump
execute if data storage midwut:main Data2.data.phys_veh{y_angle_negative_control:"use"} run tag @s add phys_veh.add_angle.y.negative_use
execute if data storage midwut:main Data2.data.phys_veh{x_angle_negative_control:"w"} run tag @s add phys_veh.add_angle.x.negative_w
execute if data storage midwut:main Data2.data.phys_veh{x_angle_negative_control:"a"} run tag @s add phys_veh.add_angle.x.negative_a
execute if data storage midwut:main Data2.data.phys_veh{x_angle_negative_control:"s"} run tag @s add phys_veh.add_angle.x.negative_s
execute if data storage midwut:main Data2.data.phys_veh{x_angle_negative_control:"d"} run tag @s add phys_veh.add_angle.x.negative_d
execute if data storage midwut:main Data2.data.phys_veh{x_angle_negative_control:"sprint"} run tag @s add phys_veh.add_angle.x.negative_sprint
execute if data storage midwut:main Data2.data.phys_veh{x_angle_negative_control:"jump"} run tag @s add phys_veh.add_angle.x.negative_jump
execute if data storage midwut:main Data2.data.phys_veh{x_angle_negative_control:"use"} run tag @s add phys_veh.add_angle.x.negative_use
execute if data storage midwut:main Data2.data.phys_veh{z_angle_negative_control:"w"} run tag @s add phys_veh.add_angle.z.negative_w
execute if data storage midwut:main Data2.data.phys_veh{z_angle_negative_control:"a"} run tag @s add phys_veh.add_angle.z.negative_a
execute if data storage midwut:main Data2.data.phys_veh{z_angle_negative_control:"s"} run tag @s add phys_veh.add_angle.z.negative_s
execute if data storage midwut:main Data2.data.phys_veh{z_angle_negative_control:"d"} run tag @s add phys_veh.add_angle.z.negative_d
execute if data storage midwut:main Data2.data.phys_veh{z_angle_negative_control:"sprint"} run tag @s add phys_veh.add_angle.z.negative_sprint
execute if data storage midwut:main Data2.data.phys_veh{z_angle_negative_control:"jump"} run tag @s add phys_veh.add_angle.z.negative_jump
execute if data storage midwut:main Data2.data.phys_veh{z_angle_negative_control:"use"} run tag @s add phys_veh.add_angle.z.negative_use

execute if entity @s[tag=phys_veh.wheel] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/wheel
execute if entity @s[tag=phys_veh.track] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/track
execute if data storage midwut:main Data2.data.phys_veh.cannon run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/cannon
execute if data storage midwut:main Data2.data.phys_veh.aiming run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/aiming
