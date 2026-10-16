# Main
# Main
# Main
execute if data storage midwut:main midwut_crafting_table{tab:"physical_vehicles.main_tab"} run data modify storage midwut:main Tabs set value [\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_name": {\
        translate: "phys_veh.remover",\
        fallback: "Vehicles Remover"\
      },\
      "minecraft:lore": [\
        {\
          "translate": "phys_veh.remover.lore",\
          "italic": false,\
          "color": "gray"\
        },\
        {\
          "translate": "phys_veh.remover.lore2",\
          "italic": false,\
          "color": "gray"\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.remover: true\
      },\
      "!minecraft:consumable": {},\
      "minecraft:rarity": "uncommon"\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_name": {\
        translate: "phys_veh.car_key",\
        fallback: "Car Key"\
      },\
      "minecraft:lore": [\
        {\
          "translate": "phys_veh.car_key.lore",\
          "italic": false,\
          "color": "gray"\
        },\
        {\
          "translate": "phys_veh.car_key.lore2",\
          "italic": false,\
          "color": "gray"\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.car_key: true\
      },\
      "!minecraft:consumable": {}\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_name": {\
        translate: "phys_veh.torque_wrench",\
        fallback: "Torque Wrench"\
      },\
      "minecraft:lore": [\
        {\
          "translate": "phys_veh.torque_wrench.lore",\
          "italic": false,\
          "color": "gray"\
        },\
        {\
          "translate": "phys_veh.torque_wrench.lore2",\
          "italic": false,\
          "color": "gray"\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.torque_wrench: true\
      },\
      "!minecraft:consumable": {}\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_model_data": {\
        strings: ["black"]\
      },\
      "minecraft:custom_name": {\
        translate: "phys_veh.armored_truck_4x4",\
        fallback: "Armored Truck 4x4"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.model_by",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "GODZILLA256",\
              color: "#3B4675"\
            }\
          ]\
        },\
        {\
          translate: "phys_veh.choosing_skin",\
          fallback: "Choosing a Skin",\
          italic: false,\
          color: "gray"\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.armored_truck_4x4_tab: true\
      },\
      "!minecraft:consumable": {}\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_name": {\
        translate: "phys_veh.buhanka",\
        fallback: "Buhanka"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.model_by",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "DrowsyFirefox",\
              color: "#FDD3B1"\
            }\
          ]\
        },\
        {\
          translate: "phys_veh.creator-s_note",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "Сасай, чё с балконом?",\
              color: "white"\
            }\
          ]\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.buhanka: true\
      },\
      "!minecraft:consumable": {}\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_name": {\
        translate: "phys_veh.vanna",\
        fallback: "Vanna"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.model_by",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "DrowsyFirefox",\
              color: "#FDD3B1"\
            }\
          ]\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.vanna: true\
      },\
      "!minecraft:consumable": {}\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_name": {\
        translate: "phys_veh.ecto-1a",\
        fallback: "Ecto-1A"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.model_by",\
          italic: false,\
          color: "gray",\
          with: [\
            {"translate": "midwut.list.2", "with": [", ", {text: "WYLD", color: "red"}, {text: "B2", color: "#ffa500"}]}\
          ]\
        },\
        {\
          translate: "phys_veh.from_mod",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "GhostBusters",\
              color: "#E42D19"\
            }\
          ]\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.ecto-1a: true\
      },\
      "!minecraft:consumable": {}\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_model_data": {\
        colors: [16777215]\
      },\
      "minecraft:custom_name": {\
        translate: "phys_veh.hammer_monster",\
        fallback: "Hammer Monster Truck"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.model_by",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "ZCBM",\
              color: "aqua"\
            }\
          ]\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.hammer_monster: true\
      },\
      "!minecraft:consumable": {}\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_name": {\
        translate: "phys_veh.jeep_cj-5_1964",\
        fallback: "Jeep CJ-5 1964"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.model_by",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "Electric_Lights",\
              color: "#2D78D8"\
            }\
          ]\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.jeep_cj-5_1964: true\
      },\
      "!minecraft:consumable": {}\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_name": {\
        translate: "phys_veh.oldsmobile_cutlass_supreme_coupe_1980",\
        fallback: "Oldsmobile Cutlass Supreme Coupe (1980)"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.model_by",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "Antoha256M",\
              color: "#FFAA00"\
            }\
          ]\
        },\
        {\
          translate: "phys_veh.from_game",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "Detroit: Become Human",\
              color: "#51D5F9"\
            }\
          ]\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.oldsmobile_cutlass_supreme_coupe_1980: true\
      },\
      "!minecraft:consumable": {}\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_name": {\
        translate: "phys_veh.tt-66052",\
        fallback: "TT-66052"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.model_by",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "Antoha256M",\
              color: "#FFAA00"\
            }\
          ]\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.tt-66052: true\
      },\
      "!minecraft:consumable": {}\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_name": {\
        translate: "phys_veh.zil-130",\
        fallback: "ЗИЛ-130"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.model_by",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "Falanta Fantomo",\
              color: "#FEBE04"\
            }\
          ]\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.zil-130: true\
      },\
      "!minecraft:consumable": {}\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_model_data": {\
        strings: ["tint"],\
        colors: [16777215]\
      },\
      "minecraft:custom_name": {\
        translate: "phys_veh.zaz-968m",\
        fallback: "ЗАЗ-968М"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.model_by",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "Falanta Fantomo",\
              color: "#FEBE04"\
            }\
          ]\
        },\
        {\
          translate: "phys_veh.choosing_skin",\
          fallback: "Choosing a Skin",\
          italic: false,\
          color: "gray"\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.zaz-968m_tab: true\
      },\
      "!minecraft:consumable": {}\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_model_data": {\
        strings: ["tint"],\
        colors: [16777215]\
      },\
      "minecraft:custom_name": {\
        translate: "phys_veh.zaz-968a",\
        fallback: "ЗАЗ-968А"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.model_by",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "Falanta Fantomo",\
              color: "#FEBE04"\
            }\
          ]\
        },\
        {\
          translate: "phys_veh.choosing_skin",\
          fallback: "Choosing a Skin",\
          italic: false,\
          color: "gray"\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.zaz-968a_tab: true\
      },\
      "!minecraft:consumable": {}\
    }\
  }\
]

# armored_truck_4x4
# armored_truck_4x4
# armored_truck_4x4
execute if data storage midwut:main midwut_crafting_table{tab:"physical_vehicles.armored_truck_4x4_tab"} run data modify storage midwut:main Tabs set value [\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_model_data": {\
        strings: ["black"]\
      },\
      "minecraft:custom_name": {\
        translate: "phys_veh.armored_truck_4x4",\
        fallback: "Armored Truck 4x4"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.model_by",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "GODZILLA256",\
              color: "#3B4675"\
            }\
          ]\
        },\
        {\
          translate: "options.generic_value",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              translate: "phys_veh.skin",\
              fallback: "Skin"\
            },\
            {\
              translate: "phys_veh.armored_truck_4x4.skin.black",\
              fallback: "black"\
            }\
          ]\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.armored_truck_4x4_black: true\
      },\
      "!minecraft:consumable": {}\
    }\
  },\
  {\
    id: "minecraft:poisonous_potato",\
    Slot: -1b,\
    components: {\
      \
      "minecraft:custom_model_data": {\
        strings: ["gray-red"]\
      },\
      "minecraft:custom_name": {\
        translate: "phys_veh.armored_truck_4x4",\
        fallback: "Armored Truck 4x4"\
      },\
      "minecraft:lore": [\
        {\
          translate: "phys_veh.model_by",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              text: "GODZILLA256",\
              color: "#3B4675"\
            }\
          ]\
        },\
        {\
          translate: "options.generic_value",\
          italic: false,\
          color: "gray",\
          with: [\
            {\
              translate: "phys_veh.skin",\
              fallback: "Skin"\
            },\
            {\
              translate: "phys_veh.armored_truck_4x4.skin.gray-red",\
              fallback: "gray-red"\
            }\
          ]\
        }\
      ],\
      "minecraft:custom_data": {\
        midwut.gui_item: true,\
        kill: true,\
        physical_vehicles.armored_truck_4x4_gray-red: true\
      },\
      "!minecraft:consumable": {}\
    }\
  }\
]

# zaz-968m
# zaz-968m
# zaz-968m
execute if data storage midwut:main midwut_crafting_table{tab:"physical_vehicles.zaz-968m_tab"} run function physical_vehicles:zzz/midwut_crafting_table/tabs/zaz-968m

# zaz-968a
# zaz-968a
# zaz-968a
execute if data storage midwut:main midwut_crafting_table{tab:"physical_vehicles.zaz-968a_tab"} run function physical_vehicles:zzz/midwut_crafting_table/tabs/zaz-968a