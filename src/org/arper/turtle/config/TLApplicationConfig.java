package org.arper.turtle.config;


public interface TLApplicationConfig {

    TLAnglePolicy getAnglePolicy();

    int getCanvasWidth();
    int getCanvasHeight();

    int getSimulationCores();
    long getSimulationStepMicros();
    long getSimulationMaxStutterMicros();
    long getSimulationMaxBusyWaitMicros();

    TLApplicationConfig DEFAULT = new TLApplicationConfig() {

        @Override
        public TLAnglePolicy getAnglePolicy() {
            return TLAnglePolicy.Degrees;
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
        public long getSimulationMaxStutterMicros() {
            return 5000L;
        }
        
        @Override
        public long getSimulationMaxBusyWaitMicros() {
            return 50;
        }

    };

}
