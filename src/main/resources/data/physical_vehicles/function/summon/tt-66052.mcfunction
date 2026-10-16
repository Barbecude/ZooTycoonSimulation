# Main
summon minecraft:text_display ~ ~1.0 ~ {data:{phys_veh:{rolling_in_air:10d,mass:24000,gravity:[0d,-11.03d,0d],bounding_box:[1.75d, 1.75d, 4.5d],mass_center:[0.0f,0.8f,-0.5f],steering_angle:[36d,36d,1.75d,77d,27.5d],base_radius:0.578d,aligning_torque_divider:100,\
sounds:[\
  {id:"horn",sound_id:"physical_vehicles:other.horn.2",duration:2s,volume:5f,pitch:1f},\
  {id:"switching_gear_start",sound_id:"physical_vehicles:other.switching_gear.start",volume:0.5f,pitch:1f},\
  {id:"switching_gear_finish",sound_id:"physical_vehicles:other.switching_gear.finish",volume:0.5f,pitch:1f},\
  {id:"ignition_start",sound_id:"physical_vehicles:other.ignition_start",duration:5s,volume:1.0f,pitch:1.0f},\
  {id:"ignition_starting",sound_id:"physical_vehicles:other.ignition_starting",duration:2s,volume:3.0f,pitch:1.0f},\
  {id:"engine",sound_id:"physical_vehicles:other.engine.0",duration:2s,base_volume:5.0f,base_rpm:5000,multiplier:80},\
  {id:"handbrake_tightened",sound_id:"physical_vehicles:other.handbrake_truck_tightened",volume:1f,pitch:1f},\
  {id:"handbrake_removed",sound_id:"physical_vehicles:other.handbrake_truck_removed",volume:1f,pitch:1f}\
],\
gears:[-9.0d, 0d, 9.5d, 6.58d, 4.68d, 3.48d, 2.62d, 1.89d, 1.35d, 1.0d, 0.75d],\
engine:{friction:[40.0d, 0.15d, 0.002801d],idle_rpm:700,min_rpm:500,redline_rpm:3500,torque_curve_coeff:3,max_torque:1087,max_torque_rpm:1300,moment_of_inertia_reversed:0.9,rpm_scaling_factor:100},\
transmission:{max_torque:2000,stiffness:500,final_gear:5.9d,gearbox_efficiency:0.92d},\
collisions:[\
[1.312d, -0.093d, 4.188d],\
[0.438d, -0.093d, 4.188d],\
[-0.438d, -0.093d, 4.188d],\
[-1.312d, -0.093d, 4.188d],\
[1.312d, 0.907d, 4.188d],\
[0.438d, 0.907d, 4.188d],\
[-0.438d, 0.907d, 4.188d],\
[-1.312d, 0.907d, 4.188d],\
[1.312d, 1.532d, 4.0d],\
[0.0d, 1.532d, 4.0d],\
[-1.312d, 1.532d, 4.0d],\
[1.25d, 2.469d, 3.625d],\
[0.0d, 2.469d, 3.625d],\
[-1.25d, 2.469d, 3.625d],\
[1.25d, 2.469d, 2.625d],\
[0.0d, 2.469d, 2.625d],\
[-1.25d, 2.469d, 2.625d],\
[1.312d, 0.969d, 2.438d],\
[0.0d, 0.969d, 2.438d],\
[-1.312d, 0.969d, 2.438d],\
[1.281d, -0.281d, 2.75d],\
[-1.281d, -0.281d, 2.75d],\
[0.0d, -0.218d, 2.75d],\
[0.0d, -0.218d, -1.344d],\
[0.0d, 0.094d, -0.094d],\
[0.0d, 0.094d, 1.156d],\
[0.0d, -0.218d, -2.844d],\
[1.188d, -0.156d, -5.125d],\
[0.438d, -0.156d, -5.125d],\
[-0.438d, -0.156d, -5.125d],\
[-1.188d, -0.156d, -5.125d],\
[1.312d, 1.407d, -4.812d],\
[-1.312d, 1.407d, -4.812d],\
[0.938d, 2.282d, -4.812d],\
[-0.938d, 2.282d, -4.812d],\
[0.0d, 2.532d, -4.812d],\
[1.312d, 1.407d, -2.75d],\
[0.938d, 2.282d, -2.75d],\
[0.0d, 2.532d, -2.75d],\
[-0.938d, 2.282d, -2.75d],\
[-1.312d, 1.407d, -2.75d],\
[1.312d, 1.407d, -0.75d],\
[0.938d, 2.282d, -0.75d],\
[0.0d, 2.532d, -0.75d],\
[-0.938d, 2.282d, -0.75d],\
[-1.312d, 1.407d, -0.75d],\
[1.312d, 1.407d, 1.312d],\
[-1.312d, 1.407d, 1.312d],\
[0.938d, 2.282d, 1.312d],\
[-0.938d, 2.282d, 1.312d],\
[0.0d, 2.532d, 1.312d],\
[0.5d, 2.782d, 0.375d],\
[-0.5d, 2.782d, 0.375d],\
[-0.5d, 2.782d, -1.75d],\
[0.5d, 2.782d, -1.75d],\
[-0.5d, 2.782d, -3.812d],\
[0.5d, 2.782d, -3.812d],\
[-1.281d, -0.281d, -1.344d],\
[1.281d, -0.281d, -1.344d],\
[-1.281d, -0.281d, -2.844d],\
[1.281d, -0.281d, -2.844d],\
],collision_box:[-1.375d, -0.218d, -5.062d, 1.375d, 2.657d, 4.188d],coeff_e:0.05d,friction_reduction_ratio:0.5d,\
armor_collisions:[{box:[-1.375d, -0.218d, -5.062d, 1.375d, 2.657d, 4.188d],thickness:5,components:[{id:"engine",chance:0.3},{id:"tank",chance:0.2}]}]\
}},Tags:["phys_veh.tt-66052","physical_vehicle","get_id","phys_veh.main","phys_veh.car","phys_veh.have_collision","phys_veh.have_locking_center_differential","phys_veh.have_locking_inter_wheel_differential","phys_veh.debug._particles"]}

# Engine hitbox
summon minecraft:text_display ~ ~ ~ {data:{phys_veh:{offset:[0.0d,0.063d,2.5d]}},Passengers:[{id:"minecraft:rabbit",DeathLootTable:"",Silent:1b,Health:1024f,attributes:[{id:"minecraft:max_health",base:1024d}],NoAI:1b,Tags:["phys_veh.tt-66052","physical_vehicle","ignore.passable_leaves","phys_veh.disable.rotation","phys_veh.disable.offset","get_id","phys_veh.hp"],NoGravity:1b,active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b}]}],Tags:["phys_veh.tt-66052","physical_vehicle","phys_veh.disable.rotation","get_id","phys_veh.hp"]}

# Seats
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[0.812d,1.0d,3.0d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:10d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.tt-66052","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.llama_seat_dr","phys_veh.disable.offset"]}],Tags:["phys_veh.tt-66052","physical_vehicle","get_id","phys_veh.seat_dr","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[-0.812d,1.0d,3.0d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:10d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.tt-66052","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.tt-66052","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}

# Model
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0d,1.344d,0d]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10016}},Tags:["phys_veh.tt-66052","physical_vehicle","get_id","phys_veh.model"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[4.0f,4.0f,4.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0.812d, 1.505d, 3.577d]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10017}},teleport_duration:1,interpolation_duration:1,Tags:["phys_veh.tt-66052","physical_vehicle","get_id","phys_veh.steering_wheel"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f]}}

# Wheels
summon item_display ~ ~ ~ {data:{phys_veh:{mass:120,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.15d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:4500,offset:[1.156d, 0.0d, 2.766d],longitudinal:{stiffness:1.9d,shape_factor:1.65d,curvature:-20d,peak_factor:0.95d},lateral:{stiffness:1.7d,shape_factor:1.5d,curvature:-15d,peak_factor:0.9d},aligning_torque:{stiffness:3d,shape_factor:2d,curvature:-5d,peak_factor:0.15d},inertia_radius:0.562d,radius:0.578d,tread_height:0.218d,suspension:{strength:65000,damping:70000,height:0.172d,max_delta:0.2d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10018}},Tags:["phys_veh.tt-66052","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:120,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.15d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:4500,offset:[-1.156d, 0.0d, 2.766d],longitudinal:{stiffness:1.9d,shape_factor:1.65d,curvature:-20d,peak_factor:0.95d},lateral:{stiffness:1.7d,shape_factor:1.5d,curvature:-15d,peak_factor:0.9d},aligning_torque:{stiffness:3d,shape_factor:2d,curvature:-5d,peak_factor:0.15d},inertia_radius:0.562d,radius:0.578d,tread_height:0.218d,suspension:{strength:65000,damping:70000,height:0.172d,max_delta:0.2d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10019}},Tags:["phys_veh.tt-66052","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:120,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.15d,heat_capacity:2000,temperature:25,temperature0:25,wheel_axle_id:0,brakes_torque:2500,hand_brake_torque:4000,offset:[-1.031d, 0.0d, -1.344d],longitudinal:{stiffness:2.7d,shape_factor:1.65d,curvature:-5d,peak_factor:1.0d},lateral:{stiffness:2.2d,shape_factor:1.5d,curvature:-15d,peak_factor:0.85d},inertia_radius:0.562d,radius:0.578d,tread_height:0.218d,suspension:{strength:65000,damping:70000,height:0.125d,max_delta:0.2d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10020}},Tags:["phys_veh.tt-66052","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:120,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.15d,heat_capacity:2000,temperature:25,temperature0:25,wheel_axle_id:0,brakes_torque:2500,hand_brake_torque:4000,offset:[1.031d, 0.0d, -1.344d],longitudinal:{stiffness:2.7d,shape_factor:1.65d,curvature:-5d,peak_factor:1.0d},lateral:{stiffness:2.2d,shape_factor:1.5d,curvature:-15d,peak_factor:0.85d},inertia_radius:0.562d,radius:0.578d,tread_height:0.218d,suspension:{strength:65000,damping:70000,height:0.125d,max_delta:0.2d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10020}},Tags:["phys_veh.tt-66052","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:120,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.15d,heat_capacity:2000,temperature:25,temperature0:25,wheel_axle_id:1,brakes_torque:2500,hand_brake_torque:4000,offset:[-1.031d, 0.0d, -2.844d],longitudinal:{stiffness:2.7d,shape_factor:1.65d,curvature:-5d,peak_factor:1.0d},lateral:{stiffness:2.2d,shape_factor:1.5d,curvature:-15d,peak_factor:0.85d},inertia_radius:0.562d,radius:0.578d,tread_height:0.218d,suspension:{strength:65000,damping:70000,height:0.125d,max_delta:0.2d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10020}},Tags:["phys_veh.tt-66052","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:120,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.15d,heat_capacity:2000,temperature:25,temperature0:25,wheel_axle_id:1,brakes_torque:2500,hand_brake_torque:4000,offset:[1.031d, 0.0d, -2.844d],longitudinal:{stiffness:2.7d,shape_factor:1.65d,curvature:-5d,peak_factor:1.0d},lateral:{stiffness:2.2d,shape_factor:1.5d,curvature:-15d,peak_factor:0.85d},inertia_radius:0.562d,radius:0.578d,tread_height:0.218d,suspension:{strength:65000,damping:70000,height:0.125d,max_delta:0.2d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10020}},Tags:["phys_veh.tt-66052","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}

# Custom Properties
#scoreboard players set @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.gravity.y 0
#scoreboard players set @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.add_angle.x 7853
#scoreboard players set @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.add_angle.y 7854
#scoreboard players set @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.add_angle.z 15708
#tp @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] ~ ~5 ~

# Data
function physical_vehicles:zzz/summon_data/main
