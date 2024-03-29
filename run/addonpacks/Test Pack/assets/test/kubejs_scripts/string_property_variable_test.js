// Firstly, register the property in the entity
PalladiumEvents.registerPropertiesClientSided((event) => {
  // Only register for players
  if (event.getEntityType() === "minecraft:player") {
      // Arguments: Key of the property, type of the property, default/starting value
      event.registerProperty('standing_on_block', 'string', 'minecraft:air');
    }
});