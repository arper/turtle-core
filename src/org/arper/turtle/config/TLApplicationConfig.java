package org.arper.turtle.config;


public interface TLApplicationConfig {

    AnglePolicy getAnglePolicy();

    int getCanvasWidth();
    int getCanvasHeight();

    int getSimulationCores();
    long getSimulationStepMicros();
    long getSimulationMaxBusyWaitMicros();

    TLApplicationConfig DEFAULT = new TLApplicationConfig() {

        @Override
        public AnglePolicy getAnglePolicy() {
            return AnglePolicy.Degrees;
        }

        @Override
        public int getCanvasWidth() {
            return 800;
        }

        @Override
        public int getCanvasHeight() {
            return 600;
        }

        @Override
        public int getSimulationCores() {
            return 4;
        }

        @Override
        public long getSimulationStepMicros() {
            return 1000L;
        }

        @Override
        public long getSimulationMaxBusyWaitMicros() {
            return 50;
        }

    };

}
