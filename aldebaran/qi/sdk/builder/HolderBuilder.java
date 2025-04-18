package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.object.autonomousabilities.AutonomousAbilityHolder;
import com.aldebaran.qi.sdk.object.autonomousabilities.DegreeOfFreedom;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;

/**
 * Build a new {@link Holder}
 */
public class HolderBuilder {

    private List<AutonomousAbilitiesType> types;
    private List<DegreeOfFreedom> degrees;
    private QiContext context;

    private HolderBuilder(QiContext context) {
        this.context = context;
        this.types = new ArrayList<>();
        this.degrees = new ArrayList<>();
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the context
     * @return the builder
     */
    public static HolderBuilder with(QiContext context) {
        return new HolderBuilder(context);
    }

    /**
     * Configure the autonomous abilities to hold
     *
     * @param types the autonomous abilities types
     * @return the builder
     */
    public HolderBuilder withAutonomousAbilities(AutonomousAbilitiesType... types) {
        if (types != null) {
            this.types.addAll(asList(types));
        }
        return this;
    }

    /**
     * Configure the degrees of freedom to hold
     *
     * @param degrees the degrees of freedom
     * @return the builder
     */
    public HolderBuilder withDegreesOfFreedom(DegreeOfFreedom... degrees) {
        if (degrees != null) {
            this.degrees.addAll(asList(degrees));
        }
        return this;
    }

    /**
     * Return a configured instance of Handle
     *
     * @return the Handle
     */
    public Holder build() {
        if (types.isEmpty() && degrees.isEmpty()) {
            String text = "At least one AutonomousAbilitiesType or DegreeOfFreedom is required";
            throw new IllegalStateException(text);
        }

        List<Callable<AutonomousAbilityHolder>> callables = new ArrayList<>();

        for (AutonomousAbilitiesType type : types) {
            switch (type) {
                case BASIC_AWARENESS:
                    callables.add(() -> context.getAutonomousAbilities().holdBasicAwareness(context.getRobotContext()));
                    break;
                case BACKGROUND_MOVEMENT:
                    callables.add(() -> context.getAutonomousAbilities().holdBackgroundMovement(context.getRobotContext()));
                    break;
                case AUTONOMOUS_BLINKING:
                    callables.add(() -> context.getAutonomousAbilities().holdAutonomousBlinking(context.getRobotContext()));
                    break;
                case UNSUPPORTED_ABILITIES:
                    callables.add(() -> context.getAutonomousAbilities().holdUnsupportedAbilities(context.getRobotContext(), QiSDK.VERSION));
                    break;
            }
        }

        if (!degrees.isEmpty()) {
            if (degrees.contains(DegreeOfFreedom.UNSUPPORTED_VALUE)) {
                throw new IllegalStateException("UNSUPPORTED_VALUE cannot be used to hold DegreesOfFreedom");
            }
            callables.add(() -> context.getAutonomousAbilities().holdByDegreesOfFreedom(context.getRobotContext(), degrees));
        }

        return new Holder(callables);
    }
}
