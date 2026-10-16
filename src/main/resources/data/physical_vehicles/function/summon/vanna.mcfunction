# Main
summon minecraft:text_display ~ ~1 ~ {data:{phys_veh:{rolling_in_air:40d,mass:200,gravity:[0d,-11.03d,0d],bounding_box:[0.6d, 0.7d, 1.1d],mass_center:[0.0f,0.25f,0.125f],steering_angle:[30d,30d,-2.5f,67.5d,-2.2d],base_radius:0.218d,aligning_torque_divider:10,\
sounds:[\
  {id:"horn",sound_id:"physical_vehicles:other.horn.1",duration:4s,volume:5f,pitch:1f},\
  {id:"switching_gear_start",sound_id:"physical_vehicles:other.switching_gear.start",volume:0.5f,pitch:1f},\
  {id:"switching_gear_finish",sound_id:"physical_vehicles:other.switching_gear.finish",volume:0.5f,pitch:1f},\
  {id:"ignition_start",sound_id:"physical_vehicles:other.ignition_start",duration:5s,volume:1.0f,pitch:1.0f},\
  {id:"ignition_starting",sound_id:"physical_vehicles:other.ignition_starting",duration:2s,volume:3.0f,pitch:1.0f},\
  {id:"engine",sound_id:"physical_vehicles:other.engine.0",duration:2s,base_volume:5.0f,base_rpm:5000,multiplier:80},\
  {id:"handbrake_tightened",sound_id:"physical_vehicles:other.handbrake_tightened",volume:1f,pitch:1f},\
  {id:"handbrake_removed",sound_id:"physical_vehicles:other.handbrake_removed",volume:1f,pitch:1f}\
],\
gears:[-5.0, 0.0, 10.0],\
engine:{friction:[1.0d, 0.01d, 0.0d],idle_rpm:1800,min_rpm:1200,redline_rpm:6000,torque_curve_coeff:2,max_torque:6,max_torque_rpm:5000,moment_of_inertia_reversed:50,rpm_scaling_factor:1000},\
transmission:{max_torque:50,stiffness:10,final_gear:1d,gearbox_efficiency:0.9d},\
collisions:[\
[0.688d, -0.063d, 0.812d],\
[-0.688d, -0.063d, 0.812d],\
[0.0d, -0.063d, -0.797d],\
[0.438d, 0.031d, 1.125d],\
[0.5d, 0.781d, 1.188d],\
[-0.5d, 0.781d, 1.188d],\
[-0.5d, 0.781d, 0.312d],\
[-0.5d, 0.781d, -0.562d],\
[0.5d, 0.781d, -0.562d],\
[-0.438d, 0.031d, 1.125d],\
[0.438d, 0.031d, 0.312d],\
[-0.438d, 0.031d, 0.312d],\
[0.438d, 0.031d, -0.5d],\
[-0.438d, 0.031d, -0.5d],\
[0.0d, 1.031d, -1.0d],\
[0.0d, 0.344d, -1.0d],\
[0.0d, 1.344d, -0.062d],\
],collision_box:[-0.5d, -0.156d, -1.0d, 0.5d, 1.031d, 1.188d],coeff_e:0.07d,friction_reduction_ratio:70.0d,\
armor_collisions:[{box:[-0.5d, -0.156d, -1.0d, 0.5d, 1.031d, 1.188d],thickness:5,components:[{id:"engine",chance:0.3},{id:"tank",chance:0.2}]}]\
}},Tags:["phys_veh.vanna","physical_vehicle","get_id","phys_veh.main","phys_veh.car","phys_veh.have_collision","phys_veh.debug._particles"]}

# Engine hitbox
summon minecraft:text_display ~ ~ ~ {data:{phys_veh:{offset:[0.0d,0.5d,-0.813d]}},Passengers:[{id:"minecraft:rabbit",DeathLootTable:"",Silent:1b,Health:1024f,attributes:[{id:"minecraft:max_health",base:1024d}],NoAI:1b,Tags:["phys_veh.vanna","physical_vehicle","ignore.passable_leaves","phys_veh.disable.rotation","phys_veh.disable.offset","get_id","phys_veh.hp"],NoGravity:1b,active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b}]}],Tags:["phys_veh.vanna","physical_vehicle","phys_veh.disable.rotation","get_id","phys_veh.hp"]}

# Seats
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[0.0d,0.313d,-0.063d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.vanna","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.llama_seat_dr","phys_veh.disable.offset"]}],Tags:["phys_veh.vanna","physical_vehicle","get_id","phys_veh.seat_dr","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}

# Model
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0f,0.531f,0f]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10021}},Tags:["phys_veh.vanna","physical_vehicle","get_id","phys_veh.model"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0.0d, 0.95d, 0.515d]}},transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f]},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10022}},teleport_duration:1,interpolation_duration:1,Tags:["phys_veh.vanna","physical_vehicle","get_id","phys_veh.steering_wheel"]}

# Wheels
summon item_display ~ ~ ~ {data:{phys_veh:{mass:2,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:1d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:100,hand_brake_torque:200,offset:[0.594d, 0.0d, 0.796d],longitudinal:{stiffness:1.0d,shape_factor:1.7d,curvature:0.5d,peak_factor:0.65d},lateral:{stiffness:0.8d,shape_factor:1.6d,curvature:-0.8d,peak_factor:0.6d},inertia_radius:0.218d,radius:0.218d,tread_height:0.094d,suspension:{strength:1000,damping:3000,height:0.125d,max_delta:0.1d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10023}},Tags:["phys_veh.vanna","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.hand_brake","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:2,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:1d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:100,hand_brake_torque:200,offset:[-0.594d, 0.0d, 0.796d],longitudinal:{stiffness:1.0d,shape_factor:1.7d,curvature:0.5d,peak_factor:0.65d},lateral:{stiffness:0.8d,shape_factor:1.6d,curvature:-0.8d,peak_factor:0.6d},inertia_radius:0.218d,radius:0.218d,tread_height:0.094d,suspension:{strength:1000,damping:3000,height:0.125d,max_delta:0.1d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10023}},Tags:["phys_veh.vanna","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.hand_brake","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:2,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:1d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:200,offset:[0.0d, 0.0d, -0.796d],longitudinal:{stiffness:1.0d,shape_factor:1.7d,curvature:0.5d,peak_factor:0.7d},lateral:{stiffness:0.8d,shape_factor:1.6d,curvature:-0.8d,peak_factor:0.7d},aligning_torque:{stiffness:6d,shape_factor:2d,curvature:0.5d,peak_factor:0.4d},inertia_radius:0.218d,radius:0.218d,tread_height:0.094d,suspension:{strength:1500,damping:4000,height:0.125d,max_delta:0.1d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10024}},Tags:["phys_veh.vanna","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.steering","phys_veh.wheel.driving","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}

# Custom Properties
#scoreboard players set @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.gravity.y 0

# Data
function physical_vehicles:zzz/summon_data/main
