package org.firstinspires.ftc.teamcode.constructors;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class CRingIntake {

    // Motor that raises and lowers the intake mechanism.
    private DcMotor intakeArm       = null;

    // Servo that clamps onto rings in the intake.
    private Servo   ringClamp       = null;

    // Servo that rotates the mechanism that clamps onto rings.
    private Servo   clampRotator    = null;

    private ElapsedTime intakeTimer   = new ElapsedTime();

    private ElapsedTime clampTimer1   = new ElapsedTime();
    private ElapsedTime clampTimer2   = new ElapsedTime();

    private boolean clampTimer1Reset = false;
    private boolean clampTimer2Reset = false;

    private final double RING_CLAMP_ENGAGED = .75;
    private final double RING_CLAMP_DISENGAGED = 1;

    private final double CLAMP_ROTATOR_EXTENDED = .85;
    private final double CLAMP_ROTATOR_RETRACTED = .22;

    private final int RING_INTAKE_VERTICAL_COUNTS = -104;
    private final int RING_INTAKE_HOVERING_COUNTS = -278;
    private final int RING_INTAKE_IN_BOX_COUNTS = -30;

    private final double RING_INTAKE_TRANSITION_TO_BOX_POWER = .425;
    private final double RING_INTAKE_TRANSITION_OUT_OF_BOX_POWER = -.3;
    private final double RING_INTAKE_TRANSITION_VERTICAL_TO_IN_BOX_POWER = .1;

    private final int RING_INTAKE_SLIGHTLY_PAST_VERTICAL_TO_DOWN_COUNTS = -150;
    private final int RING_INTAKE_SLIGHTLY_PAST_VERTICAL_TO_HOVERING_COUNTS = -120;

    private final double RING_INTAKE_IN_BOX_TO_HOVERING_POWER_SLOPE = -0.00287;
    private final double RING_INTAKE_IN_BOX_TO_HOVERING_POWER_Y_INTERCEPT = -.545;

    private final double RING_INTAKE_HOVERING_POWER = .254;

    private final int EXTEND_INTAKE_MS = 500;
    private final int RETRACT_INTAKE_MS = 500; // 800;

    private final double EXTEND_INTAKE_POWER = -.425;
    private final double RETRACT_INTAKE_POWER = .6;

    private final double SERVOS_OFF = 0;

    private boolean transitionBusy = false;

    private final double COUNTERACT_GRAVITY_POWER = .1;

    private final double COUNTERACT_BOX_FORCE_POWER = 0;

    public enum IntakeArmPosition {
        IN_BOX,
        HOVERING,
        DOWN;
    }

    public enum IntakeArmTransition {
        IN_BOX_TO_HOVERING,
        IN_BOX_TO_DOWN,
        HOVERING_TO_IN_BOX,
        HOVERING_TO_DOWN,
        DOWN_TO_IN_BOX,
        DOWN_TO_HOVERING,
        DOWN_TO_INTAKE_RING;

    }

    public IntakeArmPosition intakeArmPosition = IntakeArmPosition.IN_BOX;

    public IntakeArmPosition pastIntakeArmPosition = IntakeArmPosition.IN_BOX;

    public IntakeArmTransition intakeArmTransition = IntakeArmTransition.DOWN_TO_IN_BOX;

    private final int CLAMP_TRANSITION_MS = 500;

    public void init(HardwareMap ahwMap) {

        ringClamp       = ahwMap.get(Servo.class, "ring_clamp");
        intakeArm       = ahwMap.get(DcMotor.class, "intake_arm");
        clampRotator    = ahwMap.get(Servo.class, "clamp_rotator");

        intakeArm.setPower(0);
        intakeArm.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        intakeArm.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        intakeArm.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void clampRing() {

        ringClamp.setPosition(RING_CLAMP_ENGAGED);
    }

    public void unclampRing() {

        ringClamp.setPosition(RING_CLAMP_DISENGAGED);

    }

    public void controlIntakeArm(/*double power*/) {

        switch (intakeArmTransition) {
            case IN_BOX_TO_HOVERING:
                // Transition from in box to hovering.
                if (intakeArm.getCurrentPosition() > RING_INTAKE_SLIGHTLY_PAST_VERTICAL_TO_HOVERING_COUNTS) {
                    intakeArm.setPower(RING_INTAKE_TRANSITION_OUT_OF_BOX_POWER);
                    transitionBusy = true;
                }
                if (intakeArm.getCurrentPosition() > RING_INTAKE_HOVERING_COUNTS) {
                    intakeArm.setPower(linearPower(RING_INTAKE_IN_BOX_TO_HOVERING_POWER_SLOPE, intakeArm.getCurrentPosition(), RING_INTAKE_IN_BOX_TO_HOVERING_POWER_Y_INTERCEPT));
                    transitionBusy = true;
                }
                else if (intakeArm.getCurrentPosition() < RING_INTAKE_HOVERING_COUNTS) {
                    intakeArm.setPower(RING_INTAKE_TRANSITION_TO_BOX_POWER);
                }
                else {
                    intakeArm.setPower(RING_INTAKE_HOVERING_POWER);
                    transitionBusy = false;
                }
                break;

            case IN_BOX_TO_DOWN:
                // Transition from in box to down.
                if (intakeArm.getCurrentPosition() > RING_INTAKE_SLIGHTLY_PAST_VERTICAL_TO_DOWN_COUNTS) {
                    intakeArm.setPower(RING_INTAKE_TRANSITION_OUT_OF_BOX_POWER);
                    transitionBusy = true;

                } else {
                    intakeArm.setPower(COUNTERACT_GRAVITY_POWER);
                    transitionBusy = false;
                }
                break;

            case HOVERING_TO_IN_BOX:
                // Transition from hovering to in box.
                if (intakeArm.getCurrentPosition() < RING_INTAKE_VERTICAL_COUNTS) {
                    intakeArm.setPower(RING_INTAKE_TRANSITION_TO_BOX_POWER);
                    transitionBusy = true;

                } else if (intakeArm.getCurrentPosition() < RING_INTAKE_IN_BOX_COUNTS) {
                    intakeArm.setPower(RING_INTAKE_TRANSITION_VERTICAL_TO_IN_BOX_POWER);
                    transitionBusy = true;
                    // clampRotator.setPosition(.2);
                } else {
                    intakeArm.setPower(COUNTERACT_BOX_FORCE_POWER);
                    transitionBusy = false;
                }
                break;

            case HOVERING_TO_DOWN:
                // Transition from hovering to down.
                intakeArm.setPower(COUNTERACT_GRAVITY_POWER);
                transitionBusy = false;
                break;

            case DOWN_TO_IN_BOX:
                // Transition from down to in box.
                if (intakeArm.getCurrentPosition() < RING_INTAKE_VERTICAL_COUNTS) {
                    intakeArm.setPower(RING_INTAKE_TRANSITION_TO_BOX_POWER);
                    transitionBusy = true;


                } else if (intakeArm.getCurrentPosition() < RING_INTAKE_IN_BOX_COUNTS) {
                    intakeArm.setPower(RING_INTAKE_TRANSITION_VERTICAL_TO_IN_BOX_POWER);
                    transitionBusy = true;
                    // clampRotator.setPosition(.2);
                } else {
                    intakeArm.setPower(COUNTERACT_BOX_FORCE_POWER);
                    transitionBusy = false;
                }
                break;


            case DOWN_TO_HOVERING:
                // Transition from down to hovering.
                if (intakeArm.getCurrentPosition() < RING_INTAKE_HOVERING_COUNTS) {
                    intakeArm.setPower(RING_INTAKE_TRANSITION_TO_BOX_POWER);
                } else {
                    intakeArm.setPower(RING_INTAKE_HOVERING_POWER);
                    transitionBusy = false;
                }
                break;

            case DOWN_TO_INTAKE_RING:
                if (!clampTimer1Reset) {
                    clampTimer1.reset();
                    clampRing();
                    clampTimer1Reset = !clampTimer1Reset;
                }


                if (clampTimer1.milliseconds() > CLAMP_TRANSITION_MS) {
                    if (intakeArm.getCurrentPosition() < RING_INTAKE_VERTICAL_COUNTS) {
                        intakeArm.setPower(RING_INTAKE_TRANSITION_TO_BOX_POWER);
                        transitionBusy = true;
                    } else if (intakeArm.getCurrentPosition() < RING_INTAKE_IN_BOX_COUNTS) {
                        intakeArm.setPower(RING_INTAKE_TRANSITION_VERTICAL_TO_IN_BOX_POWER);
                        transitionBusy = true;
                    } else {
                        intakeArm.setPower(COUNTERACT_BOX_FORCE_POWER);

                        if (!clampTimer2Reset) {
                            clampTimer2.reset();
                            unclampRing();
                            clampTimer2Reset = !clampTimer2Reset;
                        }

                        if (clampTimer2.milliseconds() > CLAMP_TRANSITION_MS) {
                            intakeArmTransition = IntakeArmTransition.IN_BOX_TO_HOVERING;
                            clampTimer1Reset = !clampTimer1Reset;
                            clampTimer2Reset = !clampTimer2Reset;
                            intakeArmPosition = IntakeArmPosition.HOVERING;
                            pastIntakeArmPosition = IntakeArmPosition.HOVERING;
                        }

                    }
                }
        }
    }

    public void extendIntake() {

        intakeTimer.reset();

        intakeArm.setPower(EXTEND_INTAKE_POWER);
        while (intakeTimer.milliseconds() < EXTEND_INTAKE_MS) {}
        intakeArm.setPower(SERVOS_OFF);

    }

    public void retractIntake() {

        intakeTimer.reset();

        intakeArm.setPower(RETRACT_INTAKE_POWER);
        while (intakeTimer.milliseconds() < RETRACT_INTAKE_MS) {}
        intakeArm.setPower(SERVOS_OFF);
    }

    public int returnIntakeArmPosition() {

        return intakeArm.getCurrentPosition();
    }

    public double returnIntakeArmPower() {
        return intakeArm.getPower();
    }

    public void proportionalClampRotator() {
        int x = intakeArm.getCurrentPosition();

        if (x < -200) {
            clampRotator.setPosition(.85);
        }
        else if (x >= -200 && x <= -150) {
            clampRotator.setPosition(-0.013*x - 1.75);
        }
        else { // x > -60
            clampRotator.setPosition(.22);
        }
    }

    // TODO: Actual motor instructions in each of the 6 cases where a transition is needed
    public void intakeArmPositionUpdater() {

        if (pastIntakeArmPosition == IntakeArmPosition.DOWN) {
            if (intakeArmPosition == IntakeArmPosition.HOVERING) {
                intakeArmTransition = IntakeArmTransition.DOWN_TO_HOVERING;

            }
            else if (intakeArmPosition == IntakeArmPosition.IN_BOX) {
                intakeArmTransition = IntakeArmTransition.DOWN_TO_IN_BOX;
            }
        }
        else if (pastIntakeArmPosition == IntakeArmPosition.HOVERING) {
            if (intakeArmPosition == IntakeArmPosition.DOWN) {
                intakeArmTransition = IntakeArmTransition.HOVERING_TO_DOWN;
            }
            else if (intakeArmPosition == IntakeArmPosition.IN_BOX) {
                intakeArmTransition = IntakeArmTransition.HOVERING_TO_IN_BOX;
            }
        }
        else { /*pastIntakeArmPosition == IntakeArmPosition.IN_BOX*/
            if (intakeArmPosition == IntakeArmPosition.DOWN) {
                intakeArmTransition = IntakeArmTransition.IN_BOX_TO_DOWN;
            }
            else if (intakeArmPosition == IntakeArmPosition.HOVERING) {
                intakeArmTransition = IntakeArmTransition.IN_BOX_TO_HOVERING;
            }
        }
        controlIntakeArm();
    }

    public void intakeArmPrimer() {

        intakeTimer.reset();

        while (intakeArm.getCurrentPosition() > -104 && intakeTimer.milliseconds() < 4000) {
            intakeArm.setPower(-.3);

        }
        while (intakeArm.getCurrentPosition() < -6 && intakeTimer.milliseconds() < 4000) {
            intakeArm.setPower(.1);
            clampRotator.setPosition(.2);
        }
        intakeArm.setPower(0);

    }

    public void intakeRing() {
        clampRing();

        intakeArmTransition = IntakeArmTransition.DOWN_TO_IN_BOX;
        if (!transitionBusy) {
            unclampRing();

            intakeArmTransition = IntakeArmTransition.IN_BOX_TO_HOVERING;
        }

    }

    // return y if y=mx+b
    private double linearPower(double m, double x, double b) {
        return m*x+b;
    }


}
