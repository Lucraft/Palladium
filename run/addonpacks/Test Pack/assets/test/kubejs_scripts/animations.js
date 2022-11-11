// Register custom animations
PalladiumEvents.registerAnimations((event) => {
    event.register(
        // ID
        'test/ability_test',

        // Priority
        10,

        // is animation active?
        (entity) => {
            // only active if progress isnt 0
            const progress = getAbility(entity, 0);
            return progress > 0.0;
        },

        // animate
        (model, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks) => {
            // use the smooth function to smooth the progress out (interpolation)
            const progress = animationUtil.smooth(getAbility(entity, partialTicks));

            // arguments of those functions: <model part>, <desired rotation angle>, <progress>
            const halfPi = 1.57079632679;
            animationUtil.interpolateXRotTo(model.rightArm, model.head.xRot - halfPi, progress);
            animationUtil.interpolateYRotTo(model.rightArm, model.head.yRot, progress);
            animationUtil.interpolateZRotTo(model.rightArm, model.head.zRot, progress);
        });
});

// Helper function to easily get the animation progress of the player (if the ability is existent)
function getAbility(entity, partialTicks) {
    // gets the ability, arguments: <entity>, <power ID>, <ability key used in the json>
    // see: data/test/powers/kubejs_animation_test.json
    const ability = palladium.getAbilityEntry(entity, 'test:kubejs_animation_test', 'toggle_animation');

    // if it exists
    if (ability) {
        // get the 'value' property of the ability which stores the timer, and divide it by our max value. This will return a float value from 0.0 to 1.0 representing the state of the animation
        // lerp function here is used to create a perfect smooth animation for the inbetween tick frames
        return lerp(ability.getProperty('prev_value'), ability.getProperty('value'), partialTicks) / ability.getProperty('max_value');
    } else {
        // just return 0
        return 0.0;
    }
}

function lerp(value1, value2, amount) {
    amount = amount < 0 ? 0 : amount;
    amount = amount > 1 ? 1 : amount;
    return value1 + (value2 - value1) * amount;
}