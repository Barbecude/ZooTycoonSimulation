# Main
summon minecraft:text_display ~ ~1.25 ~ {data:{phys_veh:{rolling_in_air:14d,mass:5000,gravity:[0d,-11.03d,0d],bounding_box:[1.6f,1.6f,2.8f],mass_center:[0.0f,0.25f,0.0f],steering_angle:[30d,30d,1.6f,22.5d,23.14d],base_radius:0.906d,aligning_torque_divider:100,\
sounds:[\
  {id:"horn",sound_id:"physical_vehicles:other.horn.2",duration:2s,volume:5f,pitch:1f},\
  {id:"switching_gear_start",sound_id:"physical_vehicles:other.switching_gear.start",volume:0.5f,pitch:1f},\
  {id:"switching_gear_finish",sound_id:"physical_vehicles:other.switching_gear.finish",volume:0.5f,pitch:1f},\
  {id:"ignition_start",sound_id:"physical_vehicles:other.ignition_start",duration:5s,volume:1.0f,pitch:1.0f},\
  {id:"ignition_starting",sound_id:"physical_vehicles:other.ignition_starting",duration:2s,volume:3.0f,pitch:1.0f},\
  {id:"engine",sound_id:"physical_vehicles:other.engine.0",duration:2s,base_volume:5.0f,base_rpm:5000,multiplier:80},\
  {id:"handbrake_tightened",sound_id:"physical_vehicles:other.handbrake_tightened",volume:1f,pitch:1f},\
  {id:"handbrake_removed",sound_id:"physical_vehicles:other.handbrake_removed",volume:1f,pitch:1f}\
],\
gears:[-4.2d, 0.0d, 5.8d, 3.6d, 2.4d, 1.6d, 1.0d],\
engine:{friction:[50.0d, 0.2d, 0.00201d],idle_rpm:900,min_rpm:600,redline_rpm:6500,torque_curve_coeff:3,max_torque:1500,max_torque_rpm:3500,moment_of_inertia_reversed:1,rpm_scaling_factor:100},\
transmission:{max_torque:4000,stiffness:500,final_gear:6.0d,gearbox_efficiency:0.9d},\
collisions:[\
[2.0d, 0.0d, 2.562d],\
[2.0d, 0.0d, -2.562d],\
[-2.0d, 0.0d, -2.562d],\
[-2.0d, 0.0d, 2.562d],\
[-1.438d, 1.625d, 3.438d],\
[-0.5d, 1.75d, 3.438d],\
[0.5d, 1.75d, 3.438d],\
[1.438d, 1.625d, 3.438d],\
[0.0d, 0.812d, 3.438d],\
[0.0d, 0.688d, 2.438d],\
[0.0d, 0.75d, -2.875d],\
[0.0d, 0.75d, -1.875d],\
[1.438d, 0.688d, -1.312d],\
[-1.438d, 0.688d, -1.312d],\
[1.438d, 1.562d, -1.688d],\
[-1.438d, 1.562d, -1.688d],\
[-1.438d, 0.688d, -0.312d],\
[1.438d, 0.688d, -0.312d],\
[-1.438d, 0.688d, 0.625d],\
[1.438d, 0.688d, 0.625d],\
[-1.438d, 1.812d, 1.438d],\
[0.0d, 1.938d, 1.438d],\
[1.438d, 1.812d, 1.438d],\
[-1.438d, 0.875d, 1.438d],\
[1.438d, 0.875d, 1.438d],\
[0.5d, 2.625d, 1.531d],\
[1.438d, 2.625d, 1.281d],\
[-0.5d, 2.625d, 1.531d],\
[-1.438d, 2.625d, 1.281d],\
[-1.438d, 2.625d, 0.281d],\
[-0.5d, 2.625d, 0.281d],\
[0.5d, 2.625d, 0.281d],\
[1.438d, 2.625d, 0.281d],\
[1.438d, 2.625d, -1.406d],\
[0.5d, 2.625d, -1.406d],\
[-0.5d, 2.625d, -1.406d],\
[-1.438d, 2.625d, -1.406d],\
[1.438d, 2.625d, -2.531d],\
[0.5d, 2.625d, -2.531d],\
[-0.5d, 2.625d, -2.531d],\
[-1.438d, 2.625d, -2.531d],\
[-1.438d, 1.75d, -3.406d],\
[-0.5d, 1.75d, -3.406d],\
[0.5d, 1.75d, -3.406d],\
[1.438d, 1.75d, -3.406d],\
[0.0d, 0.0d, 0.938d],\
[0.0d, 0.0d, -0.062d],\
[0.0d, 0.0d, -1.062d],\
],collision_box:[-1.438d, -0.062d, -3.312d, 1.438d, 2.625d, 3.438d],coeff_e:0.1d,friction_reduction_ratio:4.5d,\
armor_collisions:[{box:[-1.438d, -0.062d, -3.312d, 1.438d, 2.625d, 3.438d],thickness:5,components:[{id:"engine",chance:0.3},{id:"tank",chance:0.2}]}]\
}},Tags:["phys_veh.hammer_monster","physical_vehicle","get_id","phys_veh.main","phys_veh.car","phys_veh.have_collision","phys_veh.have_locking_center_differential","phys_veh.have_locking_inter_wheel_differential","phys_veh.debug._particles"]}

# Engine hitbox
summon minecraft:text_display ~ ~ ~ {data:{phys_veh:{offset:[0.0d,1.0d,2.0d]}},Passengers:[{id:"minecraft:rabbit",DeathLootTable:"",Silent:1b,Health:1024f,attributes:[{id:"minecraft:max_health",base:1024d}],NoAI:1b,Tags:["phys_veh.hammer_monster","physical_vehicle","ignore.passable_leaves","phys_veh.disable.rotation","phys_veh.disable.offset","get_id","phys_veh.hp"],NoGravity:1b,active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b}]}],Tags:["phys_veh.hammer_monster","physical_vehicle","phys_veh.disable.rotation","get_id","phys_veh.hp"]}

# Seats
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[0.75f,1.375f,0.5f]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:9d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.hammer_monster","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.llama_seat_dr","phys_veh.disable.offset"]}],Tags:["phys_veh.hammer_monster","physical_vehicle","get_id","phys_veh.seat_dr","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[-0.75f,1.375f,0.5f]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:9d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.hammer_monster","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.hammer_monster","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}

# Model
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0f,1.5f,0f]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10007}},Tags:["phys_veh.hammer_monster","physical_vehicle","get_id","phys_veh.model","phys_veh.have_tint"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[4.0f,4.0f,4.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0.768d, 1.75d, 1.0d]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10008}},teleport_duration:1,interpolation_duration:1,Tags:["phys_veh.hammer_monster","physical_vehicle","get_id","phys_veh.steering_wheel","phys_veh.have_tint"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f]}}

# Wheels
summon item_display ~ ~ ~ {data:{phys_veh:{mass:300,pressure:1d,pressure0:1d,pressure_coeff:0.1d,smoke_factor:0.2d,heat_capacity:2000,temperature:25,temperature0:25,wheel_axle_id:0,friction_contribution_factor:0.8d,brakes_torque:9000,offset:[1.65d,-0.125d,2.55d],longitudinal:{stiffness:0.9d,shape_factor:1.6d,curvature:0.5d,peak_factor:1.1d},lateral:{stiffness:0.8d,shape_factor:1.5d,curvature:0.4d,peak_factor:1.0d},aligning_torque:{stiffness:8d,shape_factor:2d,curvature:0.4d,peak_factor:0.4d},inertia_radius:0.8d,radius:0.906d,tread_height:0.375d,suspension:{strength:6000,damping:9000,height:0.5d,max_delta:0.25d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10009}},Tags:["phys_veh.hammer_monster","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.steering"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:300,pressure:1d,pressure0:1d,pressure_coeff:0.1d,smoke_factor:0.2d,heat_capacity:2000,temperature:25,temperature0:25,wheel_axle_id:0,friction_contribution_factor:0.8d,brakes_torque:9000,offset:[-1.65d,-0.125d,2.55d],longitudinal:{stiffness:0.9d,shape_factor:1.6d,curvature:0.5d,peak_factor:1.1d},lateral:{stiffness:0.8d,shape_factor:1.5d,curvature:0.4d,peak_factor:1.0d},aligning_torque:{stiffness:8d,shape_factor:2d,curvature:0.4d,peak_factor:0.4d},inertia_radius:0.8d,radius:0.906d,tread_height:0.375d,suspension:{strength:6000,damping:9000,height:0.5d,max_delta:0.25d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10009}},Tags:["phys_veh.hammer_monster","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.steering"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:300,pressure:1d,pressure0:1d,pressure_coeff:0.1d,smoke_factor:0.2d,heat_capacity:2000,temperature:25,temperature0:25,wheel_axle_id:1,friction_contribution_factor:0.8d,brakes_torque:8000,hand_brake_torque:8000,offset:[-1.65d,-0.125d,-2.55d],longitudinal:{stiffness:0.9d,shape_factor:1.6d,curvature:0.5d,peak_factor:1.1d},lateral:{stiffness:0.8d,shape_factor:1.5d,curvature:0.4d,peak_factor:1.0d},inertia_radius:0.8d,radius:0.906d,tread_height:0.375d,suspension:{strength:6000,damping:9000,height:0.5d,max_delta:0.25d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10009}},Tags:["phys_veh.hammer_monster","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:300,pressure:1d,pressure0:1d,pressure_coeff:0.1d,smoke_factor:0.2d,heat_capacity:2000,temperature:25,temperature0:25,wheel_axle_id:1,friction_contribution_factor:0.8d,brakes_torque:8000,hand_brake_torque:8000,offset:[1.65d,-0.125d,-2.55d],longitudinal:{stiffness:0.9d,shape_factor:1.6d,curvature:0.5d,peak_factor:1.1d},lateral:{stiffness:0.8d,shape_factor:1.5d,curvature:0.4d,peak_factor:1.0d},inertia_radius:0.8d,radius:0.906d,tread_height:0.375d,suspension:{strength:6000,damping:9000,height:0.5d,max_delta:0.25d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10009}},Tags:["phys_veh.hammer_monster","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}

# Custom Properties
#scoreboard players set @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.gravity.y 0
#tp @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] ~ ~4 ~

# Data
function physical_vehicles:zzz/summon_data/main




