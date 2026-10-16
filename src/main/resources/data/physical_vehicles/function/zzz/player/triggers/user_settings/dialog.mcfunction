$dialog show @s {\
  "type": "minecraft:confirmation",\
  "title": {\
    "translate": "phys_veh.user_settings.title"\
  },\
  "inputs": [\
    {\
      "type": "minecraft:number_range",\
      "key": "placement_directions",\
      "width": 200,\
      "label": {\
        "translate": "phys_veh.user_settings.placement_directions"\
      },\
      "start": 1,\
      "end": 32,\
      "step": 1,\
      "initial": $(placement_directions)\
    },\
    {\
      "type": "minecraft:number_range",\
      "key": "full_pedal_seconds",\
      "width": 200,\
      "label": {\
        "translate": "phys_veh.user_settings.full_pedal_press"\
      },\
      "label_format": "phys_veh.gui.value_secs",\
      "start": 0.0d,\
      "end": 5.0d,\
      "step": 0.05d,\
      "initial": $(full_pedal_seconds)\
    },\
    {\
      "type": "minecraft:number_range",\
      "key": "gear_changing_cooldown",\
      "width": 200,\
      "label": {\
        "translate": "phys_veh.user_settings.auto_gear_change"\
      },\
      "label_format": "phys_veh.gui.value_secs",\
      "start": 0.0d,\
      "end": 20.0d,\
      "step": 0.05d,\
      "initial": $(gear_changing_cooldown)\
    }\
  ],\
  "can_close_with_escape": true,\
  "pause": false,\
  "yes": {\
    "label": {\
      "translate": "gui.done"\
    },\
    "action": {\
      "type": "minecraft:dynamic/run_command",\
      "template": "function physical_vehicles:zzz/player/triggers/user_settings/set $(value)"\
    }\
  },\
  "no": {\
    "label": {\
      "translate": "gui.cancel"\
    }\
  }\
}