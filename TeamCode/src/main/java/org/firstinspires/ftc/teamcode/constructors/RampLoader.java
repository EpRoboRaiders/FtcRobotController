package org.firstinspires.ftc.teamcode.constructors;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.lang.annotation.ElementType;

/**
 * Controls the kicker for the rings.
 * This class moves the kicker so that it will kick the ring onto the RampLifter.
 */
public class RampLoader extends CoreImplement {

    private Servo ringKicker = null;

    private ElapsedTime kickerTimer = new ElapsedTime();
    private final double KICKER_EXTENDED_POS  = .4;
    private final double KICKER_RETRACTED_POS = 1.0;

    private final int KICKER_KICK_MS = 500;
    private final int KICKER_RETRACT_MS = 1000;

    private enum KickerStates {
        KICKER_RETRACTED,
        KICKER_EXTENDED;
    }

    private KickerStates kickerState = KickerStates.KICKER_RETRACTED;

    @Override
    public void update() {
        switch(kickerState){
            case KICKER_RETRACTED: //no action needed when in this state
                break;
            case KICKER_EXTENDED:
                if (kickerTimer.milliseconds() > KICKER_KICK_MS) {
                    kickerState = KickerStates.KICKER_RETRACTED;
                    ringKicker.setPosition(KICKER_RETRACTED_POS);
                }
        }

    }

    @Override
    public void init(HardwareMap ahwMap) {

        ringKicker   = ahwMap.get(Servo.class, "ring_kicker");

        ringKicker.setPosition(KICKER_RETRACTED_POS);
    }

    public void swingRingKicker() {
        kickerTimer.reset();
        ringKicker.setPosition(KICKER_EXTENDED_POS);
        kickerState = KickerStates.KICKER_EXTENDED;

    }

    public boolean finished() {return kickerTimer.milliseconds() > KICKER_RETRACT_MS;}


}
